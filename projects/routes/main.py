from datetime import timedelta
from statistics import mean
import asyncio
from asyncio import run
from typing import Coroutine
import plotly.express as px  # type: ignore
import numpy as np

from constants import ROUTES, GlobalRunData, Item, Modifier, Route, RouteConfig, RouteData
from data_services import IRouteDataService, MockRouteDataService
from optimizer_services import (
    IRouteOptimizerService,
    MockRouteOptimizerService,
)
from valuers import RouteValueService

"""
Features:
* Accounts for Cost of items
* Accounts for opportunity cost of doing completions
* Accounts for opportunity cost of getting routeboxes while doing completions
"""

"""
TODO

- Stack all graphs together as one
- Account for items I can use to hedge changes
- Opportunity cost should include route box odds
- New graphs showing with only my metrics (Success Rate given # of people online)
- Add more modifiers and select them
- Perform multiple regression
- Mods to keep track of statistics (People online, completion times, route success rates, average revenue per minute)
- Mods to see anywhere when looking backwards / Upwards 

"""


async def graph_for_route(
    r: RouteData,
    data_service_t: type[IRouteDataService],
    optimizer_service_t: type[IRouteOptimizerService],
    average_route_completion_time: timedelta,
    extra_item_cost: float,
):
    data_service = data_service_t()

    average_completion_time, average_revenue_per_completion = await asyncio.gather(
        data_service.async_average_completion_time(),
        data_service.async_average_completion_income(),
    )

    global_state = GlobalRunData(
        average_completion_time, average_revenue_per_completion
    )
    awaitables: list[Coroutine[None, None, float]] = []
    count_people_online = list(range(1, 25))
    success_rates = np.arange(50, 100.5, 0.5).tolist()
    route_configs = await optimizer_service_t(global_state).async_optimal_route_configs()
    route_configs = list(filter(lambda _r: _r.route.route is not r.route, route_configs))
    for s in success_rates:
        s /= 100
        for c in count_people_online:
            run_data = RouteConfig(
                route=r,
                modifier=Modifier.NO_ITEMS,
                global_state=global_state,
                count_people_online=c,
                average_route_success_probability=s,
                average_route_completion_time=average_route_completion_time,
                extra_item_costs=extra_item_cost,
            )

            # Get routebox value including the current route config
            route_configs.append(run_data)
            all_route_data_service = RouteValueService(global_data=global_state, route_config=route_configs)
            routebox_value = await all_route_data_service.async_get_routebox_value()
            route_configs.pop()

            awaitables.append(run_data.async_ticket_value(routebox_value))

    revenues = await asyncio.gather(*awaitables)

    results: list[list[float]] = []
    i = 0
    for _ in success_rates:
        cur_results: list[float] = []
        for _ in count_people_online:
            cur_results.append(max(0, revenues[i]))
            i += 1
        results.append(cur_results)
    plot_heatmap(r.route.name, count_people_online, success_rates, results)


def plot_heatmap(title: str, x: list[int], y: list[float], results: list[list[float]]):
    fig = px.imshow( # type: ignore
        results,
        title=title,
        labels=dict(x="People Online", y="Success %", color="Revenue"),
        x=x,
        y=y,
    ) 
    fig.show()  # type: ignore

async def get_optimal_data(data_service_t: type[IRouteDataService], optimizer_t: type[IRouteOptimizerService]):
    data_service = data_service_t()


    average_completion_time, average_revenue_per_completion = await asyncio.gather(
        data_service.async_average_completion_time(),
        data_service.async_average_completion_income(),
    )

    global_state = GlobalRunData(
        average_completion_time, average_revenue_per_completion
    )

    optimizer = optimizer_t(global_state)

    optimal_route_configs = await optimizer.async_optimal_route_configs()

    print("Best Case Numbers")

    all_route_data_service = RouteValueService(global_data=global_state, route_config=optimal_route_configs)

    routebox_value_pre_risk_discount = await all_route_data_service.async_get_routebox_value(None)
    routebox_value = await all_route_data_service.async_get_routebox_value()
    run_value = await all_route_data_service.async_get_run_value()
    print(f"Runbox Value (Pre risk discount):\t{routebox_value_pre_risk_discount}")
    print(f"Runbox Value:\t{routebox_value}")
    print(f"Run value:\t{run_value}")
    print(
        f"Run rate/min:\t{run_value / global_state.average_completion_time.total_seconds() * 60}"
    )
    revenue_awaitables: list[Coroutine[None, None, float]] = [c.async_ticket_value(routebox_value_pre_risk_discount) for c in optimal_route_configs]
    revenues = await asyncio.gather(*revenue_awaitables)
    for config, revenue in zip(optimal_route_configs, revenues):
        print(f"{config.route.route.name}:\t{round(revenue)}")
    print(f"Average Routebox Revenue: {mean(revenues)}")

async def main():
    # await get_optimal_data(MockRouteDataService, MockRouteOptimizerService)

    # Graphing Functionality
    route = Route.OVERLOADED
    r = ROUTES[route]
    extra_item_cost = Item.REMOTE_ACTIVATION.value * 3

    await graph_for_route(r, MockRouteDataService, MockRouteOptimizerService, timedelta(minutes=4), extra_item_cost)



if __name__ == "__main__":
    run(main())

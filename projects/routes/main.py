from statistics import mean
import asyncio
from asyncio import run
from typing import Coroutine
import plotly.express as px  # type: ignore
import numpy as np

from constants import GlobalRunData, Modifier, RouteData
from user_data import (
    IRealRouteDataAggregator,
    RealRouteDataAggregator,
    await_get_best_case_route_config,
)
from valuers import RouteImplData, async_get_ticket_value, get_run_value

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
    global_state: GlobalRunData,
    aggregator: IRealRouteDataAggregator,
    extra_item_cost: float,
):
    awaitables: list[Coroutine[None, None, float]] = []
    count_people_online = list(range(1, 25))
    success_rates = np.arange(50, 100.5, 0.5).tolist()
    runbox_value = 0
    for s in success_rates:
        for c in count_people_online:
            average_run_completion_time = await aggregator.async_run_to_completion_time(
                r
            )
            run_data = RouteImplData(
                route=r,
                modifier=Modifier.NO_ITEMS,
                global_state=global_state,
                count_people_online=c,
                average_route_success_probability=s / 100,
                average_route_completion_time=average_run_completion_time,
                extra_item_costs=extra_item_cost,
            )
            awaitables.append(run_data.async_ticket_value(runbox_value))

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
    fig = px.imshow(
        results,
        title=title,
        labels=dict(x="People Online", y="Success %", color="Revenue"),
        x=x,
        y=y,
    )  # type: ignore
    fig.show()  # type: ignore


async def main():
    aggregator = RealRouteDataAggregator()

    average_completion_time, average_revenue_per_completion = await asyncio.gather(
        aggregator.async_average_completion_time(),
        aggregator.async_average_completion_income(),
    )

    global_state = GlobalRunData(
        average_completion_time, average_revenue_per_completion
    )

    # Graphing Functionality
    # route = Route.OVERLOADED
    # r = ROUTES[route]
    # extra_item_cost = Item.REMOTE_ACTIVATION.value * 3
    # await graph_for_route(r, global_state, aggregator, extra_item_cost)

    # average_revenue_per_second = await GlobalRunData.async_average_revenue_per_second(global_state)
    # print(f"Average revenue per minute including completions and routeboxes: \t{average_revenue_per_second*60}")

    # Best Case Numbers Functionality
    print("Best Case Numbers")
    route_config = await await_get_best_case_route_config(global_state)
    routebox_value = await async_get_ticket_value(global_state, route_config)
    run_value = get_run_value(global_state, routebox_value)
    print(f"Runbox Value:\t{routebox_value}")
    print(f"Run value:\t{run_value}")
    print(
        f"Run rate/min:\t{run_value / global_state.average_completion_time.total_seconds() * 60}"
    )
    config = await await_get_best_case_route_config(global_state)
    awaitables = [c.async_ticket_value(routebox_value) for c in config.values()]
    revenues = await asyncio.gather(*awaitables)
    for route, revenue in zip(config, revenues):
        print(f"{route.name}:\t{round(revenue)}")
    print(f"Average Routebox Revenue: {mean(revenues)}")


if __name__ == "__main__":
    run(main())

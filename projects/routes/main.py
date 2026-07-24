from datetime import timedelta
from statistics import mean
import asyncio
from asyncio import run
from dataclasses import dataclass
from enum import Enum
from abc import ABC
from typing import Coroutine
import plotly.express as px # type: ignore
import pandas as pd
import numpy as np

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

class Item(Enum):
    REMOTE_ACTIVATION = 3000
    POOF = 10_000

class Modifier(Enum):
    NO_ITEMS = 300


class IRealRouteDataAggregator(ABC):
    @classmethod
    async def async_run_to_probability_success(cls, route: RouteData) -> float: ...

    @classmethod
    async def async_run_to_completion_time(cls, route: RouteData) -> timedelta: ...

    @classmethod
    async def async_average_completion_time(cls) -> timedelta: ...

    @classmethod
    async def async_average_completion_income(cls) -> float: ...


class RealRouteDataAggregator(IRealRouteDataAggregator):
    @classmethod
    async def async_run_to_probability_success(cls, route: RouteData) -> float:
        match route.route:
            case Route.UP_AND_BACK:
                return 0.9
            case Route.WRONG_WAY:
                return 0.8
            case Route.STARGAZER:
                return 0.8
            case Route.OVERLOADED:
                return 0.6
            case Route.MR_WOLF:
                return 0.5

    @classmethod
    async def async_run_to_completion_time(cls, route: RouteData) -> timedelta:
        match route.route:
            case Route.UP_AND_BACK:
                return timedelta(minutes=1, seconds=40)
            case Route.WRONG_WAY:
                return timedelta(minutes=1)
            case Route.STARGAZER:
                return timedelta(seconds=40)
            case Route.OVERLOADED:
                return timedelta(minutes=4)
            case Route.MR_WOLF:
                return timedelta(minutes=10)

    @classmethod
    async def async_average_completion_time(cls) -> timedelta:
        return timedelta(minutes=1, seconds=7)

    @classmethod
    async def async_average_completion_income(cls) -> float:
        return 1000


class Route(Enum):
    UP_AND_BACK = "Up and Back"
    WRONG_WAY = "Wrong Way"
    STARGAZER = "Stargazer"
    OVERLOADED = "Overloaded"
    MR_WOLF = "Mr. Wolf"


@dataclass
class RouteData:
    route: Route
    entry_cost: float
    revenue_per_person: float


ROUTES: dict[Route, RouteData] = {
    Route.UP_AND_BACK: RouteData(
        route=Route.UP_AND_BACK, entry_cost=5000, revenue_per_person=1000
    ),
    Route.WRONG_WAY: RouteData(
        route=Route.WRONG_WAY,
        entry_cost=10000,
        revenue_per_person=3000,
    ),
    Route.STARGAZER: RouteData(
        route=Route.STARGAZER,
        entry_cost=20000,
        revenue_per_person=6000,
    ),
    Route.OVERLOADED: RouteData(
        route=Route.OVERLOADED,
        entry_cost=35000,
        revenue_per_person=10000,
    ),
    Route.MR_WOLF: RouteData(
        route=Route.MR_WOLF,
        entry_cost=50000,
        revenue_per_person=15000,
    ),
}


@dataclass
class GlobalRunData:
    average_completion_time: timedelta
    average_completion_ev: float

    async def async_average_revenue_per_second(self) -> float:
        return self._average_completion_revenue_per_second + await self._async_average_routeboxes_revenue_per_second()

    @property
    def _average_completion_revenue_per_second(self) -> float:
        return self.average_completion_ev / self.average_completion_time.total_seconds()
    
    async def _async_average_routeboxes_revenue_per_second(self) -> float:
        completions_per_second = timedelta(seconds=1) / self.average_completion_time
        routeboxes_per_completion = 1/8

        # DANGER
        # This function call is recursive. 
        # To know the value of a Route, we need to know the opportunity cost i.e the revenue from doing completions instead.
        # However, completions have a chance of giving routeboxes which means we need to know the value of a route. 
        # Therefore, we disable this function for all future calls. 
        # average_revenues = await await_get_average_revenues(self)
        return 0
            
        average_routebox_revenue = sum(average_revenues.values()) / len(average_revenues)
        return average_routebox_revenue * completions_per_second * routeboxes_per_completion


@dataclass
class RunData:
    route: RouteData
    modifier: Modifier
    global_state: GlobalRunData
    count_people_online: int
    extra_item_costs: float

    # For accurate numbers, populate using IRealRouteDataAggregator
    average_run_success_probability: float  # [0, 1]
    average_run_completion_time: timedelta

    async def async_ev_per_person(self) -> float:
        return (
            self.route.revenue_per_person + self.modifier.value
        ) * self.average_run_success_probability
    
    async def async_opportunity_cost(self) -> float:
        average_revenue_per_second = await self.global_state.async_average_revenue_per_second()
        return (
            average_revenue_per_second
            * self.average_run_completion_time.total_seconds()
        )

    async def async_fixed_expense(self) -> float:
        return self.route.entry_cost + await self.async_opportunity_cost()

    async def async_revenue(self) -> float:
        ev_per_person, fixed_expense = await asyncio.gather(
            self.async_ev_per_person(),
            self.async_fixed_expense(),
        )
        return (ev_per_person * self.count_people_online) - fixed_expense


async def await_get_average_revenues(global_state: GlobalRunData) -> dict[Route, float]:
    revenues: dict[Route, float] = {}
    revenues[Route.UP_AND_BACK] = await RunData(
        ROUTES[Route.UP_AND_BACK],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=15,
        average_run_success_probability=0.9,
        average_run_completion_time=timedelta(minutes=2),
        extra_item_costs=0,
    ).async_revenue()

    revenues[Route.WRONG_WAY] = await RunData(
        ROUTES[Route.WRONG_WAY],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=10,
        average_run_success_probability=.8,
        average_run_completion_time=timedelta(minutes=1, seconds=30),
        extra_item_costs=0,
    ).async_revenue()

    revenues[Route.STARGAZER] = await RunData(
        ROUTES[Route.STARGAZER],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=10,
        average_run_success_probability=.9,
        average_run_completion_time=timedelta(seconds=50),
        extra_item_costs=0,
    ).async_revenue()

    revenues[Route.OVERLOADED] = await RunData(
        ROUTES[Route.OVERLOADED],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=15,
        average_run_success_probability=.8,
        average_run_completion_time=timedelta(minutes=4),
        extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
    ).async_revenue()

    revenues[Route.MR_WOLF] = await RunData(
        ROUTES[Route.MR_WOLF],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=12,
        average_run_success_probability=.5,
        average_run_completion_time=timedelta(minutes=10),
        extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
    ).async_revenue()

    return revenues

async def graph_for_route(r: RouteData, global_state: GlobalRunData, aggregator: IRealRouteDataAggregator, extra_item_cost: float, disable_routebox_opportunity_cost_calculation: bool = False):
    awaitables: list[Coroutine[None, None, float]] = []
    count_people_online = list(range(1, 25))
    success_rates = np.arange(50, 100.5, 0.5).tolist()
    for s in success_rates:
        for c in count_people_online:
            average_run_completion_time = await aggregator.async_run_to_completion_time(r)
            run_data = RunData(
                route=r,
                modifier=Modifier.NO_ITEMS,
                global_state=global_state,
                count_people_online=c,
                average_run_success_probability=s/100,
                average_run_completion_time=average_run_completion_time,
                extra_item_costs=extra_item_cost,
            )
            awaitables.append(run_data.async_revenue())

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


def plot_heatmap(
    title: str, x: list[int], y: list[float], results: list[list[float]]
):
    fig = px.imshow(results, title=title, labels=dict(x="People Online", y="Success %", color="Revenue"), x=x, y=y)
    fig.show()


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
    revenues = await await_get_average_revenues(global_state)
    for route, revenue in revenues.items():
        print(f"{route.name}:\t{round(revenue)}")
    print(f"Average Routebox Revenue: {mean(revenues.values())}")
    


if __name__ == "__main__":
    run(main())
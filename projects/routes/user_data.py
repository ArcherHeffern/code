from abc import ABC
from datetime import timedelta

from constants import ROUTES, GlobalRunData, Item, Modifier, Route, RouteData
from valuers import RouteImplData

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

async def await_get_best_case_route_config(global_state: GlobalRunData) -> dict[Route, RouteImplData]:
    # TODO: This should eventually be dynamically populated by Aggregator
    config: dict[Route, RouteImplData] = {}
    config[Route.UP_AND_BACK] = RouteImplData(
        ROUTES[Route.UP_AND_BACK],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=15,
        average_route_success_probability=0.9,
        average_route_completion_time=timedelta(minutes=2),
        extra_item_costs=0,
    )

    config[Route.WRONG_WAY] = RouteImplData(
        ROUTES[Route.WRONG_WAY],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=10,
        average_route_success_probability=.8,
        average_route_completion_time=timedelta(minutes=1, seconds=30),
        extra_item_costs=0,
    )

    config[Route.STARGAZER] = RouteImplData(
        ROUTES[Route.STARGAZER],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=10,
        average_route_success_probability=.9,
        average_route_completion_time=timedelta(seconds=50),
        extra_item_costs=0,
    )

    config[Route.OVERLOADED] = RouteImplData(
        ROUTES[Route.OVERLOADED],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=15,
        average_route_success_probability=.8,
        average_route_completion_time=timedelta(minutes=4),
        extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
    )

    config[Route.MR_WOLF] = RouteImplData(
        ROUTES[Route.MR_WOLF],
        modifier=Modifier.NO_ITEMS,
        global_state=global_state,
        count_people_online=12,
        average_route_success_probability=.5,
        average_route_completion_time=timedelta(minutes=10),
        extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
    )

    return config
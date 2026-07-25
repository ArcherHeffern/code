from abc import ABC
import asyncio
from dataclasses import dataclass
from datetime import timedelta
from typing import Coroutine, final, override

from constants import ROUTES, GlobalRunData, Item, Modifier, Route
from valuers import RouteConfig

@dataclass
class IRouteOptimizerService(ABC):
    global_state: GlobalRunData

    async def async_optimal_route_config(self, route: Route) -> RouteConfig: ...

    @final
    async def async_optimal_route_configs(self) -> list[RouteConfig]: 
        routes: list[Route] = list(Route)
        route_config_awaitables: list[Coroutine[None, None, RouteConfig]] = []
        for route in routes:
            route_config_awaitables.append(self.async_optimal_route_config(route))
        return await asyncio.gather(*route_config_awaitables)

@final
class RealRouteOptimizerService(IRouteOptimizerService):

    """
    This uses the users's run data and runs a regression model to determine the optimal RouteConfig to maximize returns
    """
    
    @override
    async def async_optimal_route_config(self, route: Route) -> RouteConfig: 
        # Take into account people online, riskiness, items, route completion time
        ...

@final 
class MockRouteOptimizerService(IRouteOptimizerService):
    @override
    async def async_optimal_route_config(self, route: Route) -> RouteConfig: 
        best_case_route_config = await self.__await_get_best_case_route_config(self.global_state)
        return best_case_route_config[route]


    @classmethod
    async def __await_get_best_case_route_config(
        cls, global_state: GlobalRunData
    ) -> dict[Route, RouteConfig]:
        # TODO: This should eventually be dynamically populated by Aggregator
        config: dict[Route, RouteConfig] = {}
        config[Route.UP_AND_BACK] = RouteConfig(
            ROUTES[Route.UP_AND_BACK],
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=15,
            average_route_success_probability=0.9,
            average_route_completion_time=timedelta(minutes=2),
            extra_item_costs=0,
        )

        config[Route.WRONG_WAY] = RouteConfig(
            ROUTES[Route.WRONG_WAY],
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=10,
            average_route_success_probability=0.8,
            average_route_completion_time=timedelta(minutes=1, seconds=30),
            extra_item_costs=0,
        )

        config[Route.STARGAZER] = RouteConfig(
            ROUTES[Route.STARGAZER],
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=10,
            average_route_success_probability=0.9,
            average_route_completion_time=timedelta(seconds=50),
            extra_item_costs=0,
        )

        config[Route.OVERLOADED] = RouteConfig(
            ROUTES[Route.OVERLOADED],
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=15,
            average_route_success_probability=0.8,
            average_route_completion_time=timedelta(minutes=4),
            extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
        )

        config[Route.MR_WOLF] = RouteConfig(
            ROUTES[Route.MR_WOLF],
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=12,
            average_route_success_probability=0.5,
            average_route_completion_time=timedelta(minutes=10),
            extra_item_costs=Item.REMOTE_ACTIVATION.value * 3,
        )

        return config
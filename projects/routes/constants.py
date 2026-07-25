from dataclasses import dataclass
from datetime import timedelta
from enum import Enum


class Item(Enum):
    REMOTE_ACTIVATION = 3000
    POOF = 10_000


class Modifier(Enum):
    NO_ITEMS = 300


ROUTEBOX_CHANCE = 1 / 8


class Route(Enum):
    UP_AND_BACK = "Up and Back"
    WRONG_WAY = "Wrong Way"
    STARGAZER = "Stargazer"
    OVERLOADED = "Overloaded"
    MR_WOLF = "Mr. Wolf"


@dataclass
class GlobalRunData:
    average_completion_time: timedelta
    average_completion_ev: float


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
    Route.MR_WOLF: RouteData(  # type: ignore
        route=Route.MR_WOLF,
        entry_cost=50000,
        revenue_per_person=15000,
    ),
}

@dataclass
class RouteConfig:
    route: RouteData
    modifier: Modifier
    global_state: GlobalRunData
    count_people_online: int
    extra_item_costs: float

    # For accurate numbers, populate using IRealRouteDataAggregator
    average_route_success_probability: float  # [0, 1]
    average_route_completion_time: timedelta

    def _ev_per_person(self) -> float:
        return (
            self.route.revenue_per_person + self.modifier.value
        ) * self.average_route_success_probability

    def _fixed_expense(self) -> float:
        return self.route.entry_cost + self.extra_item_costs

    def return_on_route(self) -> float:
        return self._ev_per_person() * self.count_people_online - self._fixed_expense()

    async def async_ticket_value(self, routebox_value: float) -> float:
        # This is the value of this exact ticket
        # route_sheet value = route return - (time todo b / time todo a) * (run return + (runbox_probability * runbox_value))

        return self.return_on_route() - (
            self.average_route_completion_time
            / self.global_state.average_completion_time
        ) * (
            self.global_state.average_completion_ev + (ROUTEBOX_CHANCE * routebox_value)
        )
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

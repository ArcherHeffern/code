from dataclasses import dataclass
from datetime import timedelta

from constants import ROUTEBOX_CHANCE, GlobalRunData, Modifier, Route, RouteData


@dataclass
class RouteImplData:
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

    def _routeboxes_per_second(self) -> float:
        completions_per_second = (
            timedelta(seconds=1) / self.global_state.average_completion_time
        )
        routeboxes_per_completion = 1 / 8
        return completions_per_second * routeboxes_per_completion

    async def async_ticket_value(self, routebox_value: float) -> float:
        # This is the value of this exact ticket
        # route_sheet value = route return - (time todo b / time todo a) * (run return + (runbox_probability * runbox_value))

        return self.return_on_route() - (
            self.average_route_completion_time
            / self.global_state.average_completion_time
        ) * (
            self.global_state.average_completion_ev + (ROUTEBOX_CHANCE * routebox_value)
        )


async def async_get_ticket_value(
    global_data: GlobalRunData, route_config: dict[Route, RouteImplData]
) -> float:
    average_route_return = 0
    for route in route_config.values():
        average_route_return += route.return_on_route()
    average_route_return /= len(route_config)

    k = 0
    for route in route_config.values():
        k += route.average_route_completion_time / global_data.average_completion_time
    k /= len(route_config)

    return (average_route_return - k * global_data.average_completion_ev) / (
        1 + k * ROUTEBOX_CHANCE
    )


def get_run_value(global_data: GlobalRunData, routebox_value: float) -> float:
    # V_run = return on a + routebox chance * routebox value
    return global_data.average_completion_ev + ROUTEBOX_CHANCE * routebox_value

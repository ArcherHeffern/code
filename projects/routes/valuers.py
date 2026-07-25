import asyncio
from dataclasses import dataclass
from statistics import stdev
from types import CoroutineType
from typing import Optional

from constants import ROUTEBOX_CHANCE, GlobalRunData, RouteConfig


@dataclass
class RouteValueService:
    global_data: GlobalRunData
    route_config: list[RouteConfig]

    async def async_get_routebox_value(self, risk_aversion_coefficient: Optional[float] = 0.3) -> float:
        average_route_return = 0
        for route in self.route_config:
            average_route_return += route.return_on_route()
        average_route_return /= len(self.route_config)

        k = 0
        for route in self.route_config:
            k += route.average_route_completion_time / self.global_data.average_completion_time
        k /= len(self.route_config)

        V_route = (average_route_return - k * self.global_data.average_completion_ev) / (
            1 + k * ROUTEBOX_CHANCE
        )

        if risk_aversion_coefficient is not None:
            return V_route - await self._async_compute_risk_discount(risk_aversion_coefficient)
        return V_route


    async def async_get_run_value(self) -> float:
        # V_run = return on a + routebox chance * routebox value
        routebox_value = await self.async_get_routebox_value()
        return self.global_data.average_completion_ev + ROUTEBOX_CHANCE * routebox_value
    
    async def _async_compute_risk_discount(self, risk_aversion_coefficient: float) -> float:
        # V_route (Risk adjusted) = V_route - risk_aversion_coefficient * stdev(V_route)
        # This computes everthing after the first subtraction sign. 
        # Risk aversion Coefficient. Ranges from roughly [0, 2]
        routebox_value_before = await self.async_get_routebox_value(None)

        awaitables: list[CoroutineType[None, None, float]] = []
        for route_data in self.route_config:
            awaitables.append(route_data.async_ticket_value(routebox_value_before))
        ticket_values: list[float] = await asyncio.gather(*awaitables)
        
        s_dev = stdev(ticket_values)

        return risk_aversion_coefficient * s_dev

        

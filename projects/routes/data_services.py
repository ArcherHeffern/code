from abc import ABC
from datetime import timedelta
from typing import final

class IRouteDataService(ABC):
    async def async_average_completion_time(self) -> timedelta: 
        ...
    
    async def async_average_completion_income(self) -> float:
        ...
    
    async def async_average_route_completion_time(self) -> timedelta:
        ...
    
@final
class MockRouteDataService(IRouteDataService):
    async def async_average_completion_time(self) -> timedelta: 
        return timedelta(minutes=1, seconds=7)
    
    async def async_average_completion_income(self) -> float:
        return 1000

@final
class RealRouteDataService(IRouteDataService):
    ...

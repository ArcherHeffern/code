from datetime import timedelta
import unittest

from main import ROUTES, GlobalRunData, Modifier, Route, RunData

class IntegerArithmeticTestCase(unittest.IsolatedAsyncioTestCase):
    async def test_up_and_back(self): 
        global_state = GlobalRunData(
            timedelta(minutes=1, seconds=7), 1000
        )
        route = ROUTES[Route.UP_AND_BACK]
        run_data = RunData(
            route=route,
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=15,
            average_run_success_probability=0.9,
            average_run_completion_time=timedelta(minutes=2),
        )

        ev_per_person = await run_data.async_ev_per_person()

        self.assertAlmostEqual(ev_per_person, 1170, places=2)

        opportunity_cost = await run_data.async_opportunity_cost()

        self.assertAlmostEqual(opportunity_cost, 1791.04, places=2)

        revenue = await run_data.async_revenue()

        self.assertAlmostEqual(revenue, 10758.96, places=2)

    async def test_stargazer(self): 
        global_state = GlobalRunData(
            timedelta(minutes=1, seconds=7), 1000
        )
        route = ROUTES[Route.STARGAZER]
        run_data = RunData(
            route=route,
            modifier=Modifier.NO_ITEMS,
            global_state=global_state,
            count_people_online=15,
            average_run_success_probability=0.9,
            average_run_completion_time=timedelta(minutes=1),
            extra_item_costs=0,
        )

        ev_per_person = await run_data.async_ev_per_person()

        self.assertAlmostEqual(ev_per_person, 5670, places=2)

        opportunity_cost = await run_data.async_opportunity_cost()

        self.assertAlmostEqual(opportunity_cost, 895.52, places=2)

        revenue = await run_data.async_revenue()

        self.assertAlmostEqual(revenue, 64154.48, places=2)

if __name__ == '__main__':
    unittest.main()
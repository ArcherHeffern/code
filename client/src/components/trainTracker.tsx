import { Box, Table } from "@radix-ui/themes";
import { useEffect, useState } from "react";

interface DirtyTrain {
    id: string,
    route: string,
    route_color: string,
    direction: string,
    final_stop: string,
    train_arrival_time: string,
    walking_distance_meters: number,
    walking_time_seconds: number,
    when_to_leave: string,
}

interface TrainResponse {
    close_trains: DirtyTrain[]
    last_updated: string
}

function cleanTrain(dirtyTrain: DirtyTrain): Train {

    const now = new Date();
    const leave = new Date(dirtyTrain.when_to_leave);

    const diffMs = leave.getTime() - now.getTime();

    const totalSeconds = Math.floor(diffMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    return {
        ...dirtyTrain,
        train_arrival_time: new Date(dirtyTrain.train_arrival_time),
        when_to_leave: new Date(dirtyTrain.when_to_leave),
        leave_in: `${minutes}:${seconds.toString().padStart(2, "0")}`,
    }
}

interface Train {
    id: string,
    route: string,
    route_color: string,
    direction: string,
    final_stop: string,
    train_arrival_time: Date,
    walking_distance_meters: number,
    walking_time_seconds: number,
    when_to_leave: Date,

    // Computed
    leave_in: string,
}

export default function TrainTracker() {

    const [trains, setTrains] = useState<Train[]>();

    useEffect(() => {

        navigator.geolocation.getCurrentPosition((position) => {
            const params = new URLSearchParams();
            params.append("lat", position.coords.latitude.toString());
            params.append("lng", position.coords.longitude.toString());
            fetch(`http://localhost:8002/by-location/renderable/?${params}`, { method: "GET", }).then((response) => {
                response.json().then((json) => {
                    console.log(json)
                    const response: TrainResponse = json as TrainResponse;
                    const trains = response.close_trains.map(cleanTrain)
                    setTrains(trains)

                })
            }).catch((reason) => {
                console.error(reason);
            })
        }, null)
    }, []);

    return (
        <Box>
            <h1>Train Tracker</h1>
            <Table.Root>
                <Table.Header>
                    <Table.Row>
                        <Table.ColumnHeaderCell>Train</Table.ColumnHeaderCell>
                        <Table.ColumnHeaderCell>Location</Table.ColumnHeaderCell>
                        <Table.ColumnHeaderCell>Leave In</Table.ColumnHeaderCell>
                        <Table.ColumnHeaderCell>Walking Distance (M)</Table.ColumnHeaderCell>
                        <Table.ColumnHeaderCell>Arrives At</Table.ColumnHeaderCell>
                    </Table.Row>
                </Table.Header>

                <Table.Body>
                    {trains?.map((train) => {
                        return <Table.Row>
                            <Table.RowHeaderCell>{train.route}</Table.RowHeaderCell>
                            <Table.Cell>{train.direction}</Table.Cell>
                            <Table.Cell>{train.leave_in}</Table.Cell>
                            <Table.Cell>{train.walking_distance_meters}</Table.Cell>
                            <Table.Cell>{train.train_arrival_time.toLocaleTimeString()}</Table.Cell>
                        </Table.Row>

                    })}
                </Table.Body>
            </Table.Root>
        </Box >
    )

}
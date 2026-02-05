import { circleStyle, leftGroupStyle, trainTimeContainerStyle } from "../styles/train"

interface TrainTrackerItemProps {
    logoColorHex: string,
    train: string,
    street: string,
    status: string
}

export default function TrainTrackerItem(props: TrainTrackerItemProps) {
    return (
        <div style={trainTimeContainerStyle}>
            <div style={leftGroupStyle}>
                <div style={{ ...circleStyle, "backgroundColor": props.logoColorHex }}>
                    {props.train}
                </div>
                <div>{props.street}</div>
            </div >
            <div>
                {props.status}
            </div>
        </div>
    )
}
import { type CSSProperties } from "react";
export const trainTimeContainerStyle: CSSProperties = {
    display: "flex",
    flexDirection: "row",
    flexWrap: "nowrap",
    justifyContent: "space-around",
    width: "200px",
    border: "1px solid black",
    padding: "5px",
    alignItems: "center",
};

export const leftGroupStyle: CSSProperties = {
    display: "flex",
    flexDirection: "row",
    justifyContent: "flex-start",
    alignItems: "center",
};

export const middleGroupStyle: CSSProperties = {
    flexGrow: 1,
};

export const circleStyle: CSSProperties = {
    borderRadius: "50%",
    display: "grid",
    placeItems: "center",
    backgroundColor: "purple",
    fontWeight: 700,
    width: "28px",
    height: "28px",
    marginRight: "10px",
};
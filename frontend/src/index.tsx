import {createRoot} from "react-dom/client";
import {useState} from "react";
import {ScheduleResponse} from "./types/schedule";
import SemesterSelection from "./section/SemesterSelection";
import ScheduleDisplay from "./section/ScheduleDisplay";

import "./css/base.scss";

const Root = () => {
    const [schedule, setSchedule] = useState<ScheduleResponse>(null)

    return <>
        <SemesterSelection setSchedule={setSchedule} />
        <ScheduleDisplay schedule={schedule} />
    </>
}

createRoot(document.getElementById('root')).render(
    <Root />
)

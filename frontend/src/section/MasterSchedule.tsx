import {useState} from "react";
import {ScheduleResponse} from "../types/schedule";
import SemesterSelection from "./SemesterSelection";
import ScheduleDisplay from "./ScheduleDisplay";

export default () => {
    const [schedule, setSchedule] = useState<ScheduleResponse>(null)

    return <>
        <SemesterSelection schedule={schedule} setSchedule={setSchedule} />
        <ScheduleDisplay schedule={schedule} />
    </>
}

import {PropsWithChildren, useState} from "react";
import {Button, FormControl, InputLabel, MenuItem, Select} from "@mui/material";
import {Semester} from "../types/semester";
import {generateSchedule} from "../service/apiClient";
import {ScheduleResponse} from "../types/schedule";

const minWidth = 200

type SemesterSelectionProps = PropsWithChildren<{
    setSchedule: (schedule: ScheduleResponse) => void
}>

const getRange = (start: number, size: number) =>
    [...Array(size)].map((_, index) => start + index)

export default ({setSchedule}: SemesterSelectionProps) => {
    const [semester, setSemester] = useState<Semester>(Semester.fall)
    const [year, setYear] = useState(2025)

    return <>
        <FormControl sx={{minWidth: minWidth}}>
            <InputLabel id="semester-select-label">Semester</InputLabel>
            <Select
                variant="outlined"
                label="Semester" labelId="semester-select-label"
                value={semester}
                onChange={e => setSemester(e.target.value)}>
                {
                    Object.values(Semester).map(semester =>
                        <MenuItem key={semester} value={semester}>{semester}</MenuItem>
                    )
                }
            </Select>
        </FormControl>
        <FormControl sx={{minWidth: minWidth}}>
            <InputLabel id="year-select-label">Year</InputLabel>
            <Select
                variant="outlined"
                label="Year" labelId="year-select-label"
                value={year}
                onChange={e => setYear(Number(e.target.value))}>
                {
                    getRange(2025, 5).map(year =>
                        <MenuItem key={year} value={year}>{year}</MenuItem>
                    )
                }
            </Select>
        </FormControl>
        <Button
            variant="outlined"
            onClick={() => setSchedule(generateSchedule(semester, year))}
        >Generate</Button>
    </>
}

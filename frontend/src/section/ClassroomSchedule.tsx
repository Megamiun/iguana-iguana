import {useEffect, useState} from "react";
import {Box, Container, FormControl, InputLabel, MenuItem, Paper, Select, Typography} from "@mui/material";
import WeeklyCalendar from "../component/WeeklyCalendar";
import {ClassroomScheduleResponse} from "../types/schedule";
import {getClassrooms, getClassroomSchedule} from "../service/apiClient";
import {Semester} from "../types/semester";
import {ClassroomResponse} from "../types/base";

export default () => {
    const [selectedClassroomId, setSelectedClassroomId] = useState<number | null>(null)
    const [classroomSchedule, setClassroomSchedule] = useState<ClassroomScheduleResponse | null>(null)
    const [classrooms, setClassrooms] = useState<ClassroomResponse[] | null>(null)

    const loadClassrooms = async () => {
        setClassrooms((await getClassrooms()).content)
    }

    const loadClassroomSchedule = async (selectedClassroomId: number) => {
        // TODO Remove hardcoded
        setClassroomSchedule(await getClassroomSchedule(selectedClassroomId, 2024, Semester.FALL))
    }

    useEffect(() => { loadClassrooms() }, []);
    useEffect(() => { selectedClassroomId && loadClassroomSchedule(selectedClassroomId) }, [selectedClassroomId])

    if (classrooms == null)
        return <></>

    return (
        <Container maxWidth="xl">
            <Box sx={{ my: 4 }}>
                <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h4" component="h1" gutterBottom>
                        Classroom Schedule
                    </Typography>
                    <Typography variant="body1" color="text.secondary" paragraph>
                        View individual classroom weekly schedules
                    </Typography>

                    <FormControl sx={{ minWidth: 300 }}>
                        <InputLabel id="classroom-select-label">Select Classroom</InputLabel>
                        <Select
                            labelId="classroom-select-label"
                            label="Select Classroom"
                            value={selectedClassroomId ?? ''}
                            onChange={e => setSelectedClassroomId(Number(e.target.value))}
                        >
                            {classrooms.map(classroom => (
                                <MenuItem key={classroom.id} value={classroom.id}>
                                    {classroom.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Paper>

                {
                    classroomSchedule ?
                        <Box>
                            <Typography variant="h6" gutterBottom>
                                {classroomSchedule.classroomName} Weekly Schedule
                            </Typography>
                            <WeeklyCalendar timeSlots={classroomSchedule.timeSlots} />
                        </Box> :
                        <Box display="flex" justifyContent="center" p={4}>
                            <Typography variant="body1" color="text.secondary">
                                No schedule found for this classroom
                            </Typography>
                        </Box>
                }
            </Box>
        </Container>
    )
}

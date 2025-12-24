import {useEffect, useState} from "react";
import {Box, Container, FormControl, InputLabel, MenuItem, Paper, Select, Typography} from "@mui/material";
import WeeklyCalendar from "../component/WeeklyCalendar";
import {TeacherScheduleResponse} from "../types/schedule";
import {getTeachers, getTeacherSchedule} from "../service/apiClient";
import {Semester} from "../types/semester";
import {TeacherResponse} from "../types/base";

export default () => {
    const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null)
    const [teacherSchedule, setTeacherSchedule] = useState<TeacherScheduleResponse | null>(null)
    const [teachers, setTeachers] = useState<TeacherResponse[] | null>(null)

    const loadTeachers = async () => {
        setTeachers((await getTeachers()).content)
    }

    const loadTeacherSchedule = async (selectedTeacherId: number) => {
        // TODO Remove hardcoded
        setTeacherSchedule(await getTeacherSchedule(selectedTeacherId, 2024, Semester.FALL))
    }

    useEffect(() => { loadTeachers() }, []);
    useEffect(() => { selectedTeacherId && loadTeacherSchedule(selectedTeacherId) }, [selectedTeacherId])

    if (teachers == null)
        return <></>

    return (
        <Container maxWidth="xl">
            <Box sx={{ my: 4 }}>
                <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h4" component="h1" gutterBottom>
                        Teacher Schedule
                    </Typography>
                    <Typography variant="body1" color="text.secondary" paragraph>
                        View individual teacher weekly schedules
                    </Typography>

                    <FormControl sx={{ minWidth: 300 }}>
                        <InputLabel id="teacher-select-label">Select Teacher</InputLabel>
                        <Select
                            labelId="teacher-select-label"
                            label="Select Teacher"
                            value={selectedTeacherId ?? ''}
                            onChange={e => setSelectedTeacherId(Number(e.target.value))}
                        >
                            {teachers.map(teacher => (
                                <MenuItem key={teacher.id} value={teacher.id}>
                                    {
                                        // TODO Return formatted from API too
                                        teacher.firstName + " " + teacher.lastName
                                    }
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Paper>

                {
                    teacherSchedule ?
                        <Box>
                            <Typography variant="h6" gutterBottom>
                                {teacherSchedule.teacherName} Weekly Schedule
                            </Typography>
                            <WeeklyCalendar timeSlots={teacherSchedule.timeSlots} />
                        </Box> :
                        <Box display="flex" justifyContent="center" p={4}>
                            <Typography variant="body1" color="text.secondary">
                                No schedule found for this teacher
                            </Typography>
                        </Box>
                }
            </Box>
        </Container>
    )
}

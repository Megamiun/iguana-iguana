import {useEffect, useState} from "react";
import {Box, Container, FormControl, InputLabel, MenuItem, Paper, Select, Typography} from "@mui/material";
import WeeklyCalendar from "../component/WeeklyCalendar";
import {getStudents, getStudentSchedule} from "../service/apiClient";
import {Semester} from "../types/semester";
import {StudentScheduleResponse} from "../types/schedule";
import {StudentResponse} from "../types/base";

export default () => {
    const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null)
    const [studentSchedule, setStudentSchedule] = useState<StudentScheduleResponse | null>(null)
    const [students, setStudents] = useState<StudentResponse[] | null>(null)

    const loadStudents = async () => {
        setStudents((await getStudents()).content)
    }

    const loadStudentSchedule = async (selectedStudentId: number) => {
        // TODO Remove hardcoded
        setStudentSchedule(await getStudentSchedule(selectedStudentId, 2024, Semester.FALL))
    }

    useEffect(() => { loadStudents() }, []);
    useEffect(() => { selectedStudentId && loadStudentSchedule(selectedStudentId) }, [selectedStudentId])

    if (students == null)
        return <></>

    return (
        <Container maxWidth="xl">
            <Box sx={{ my: 4 }}>
                <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
                    <Typography variant="h4" component="h1" gutterBottom>
                        Student Schedule
                    </Typography>
                    <Typography variant="body1" color="text.secondary" paragraph>
                        View individual student weekly schedules
                    </Typography>

                    <FormControl sx={{ minWidth: 300 }}>
                        <InputLabel id="student-select-label">Select Student</InputLabel>
                        <Select
                            labelId="student-select-label"
                            label="Select Student"
                            value={selectedStudentId ?? ''}
                            onChange={e => setSelectedStudentId(Number(e.target.value))}
                        >
                            {students.map(student => (
                                <MenuItem key={student.id} value={student.id}>
                                    {
                                        // TODO Return formatted from API too
                                        student.firstName + " " + student.lastName
                                    }
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Paper>

                {
                    studentSchedule ?
                        <Box>
                            <Typography variant="h6" gutterBottom>
                                {studentSchedule.studentName} Weekly Schedule
                            </Typography>
                            <WeeklyCalendar timeSlots={studentSchedule.timeSlots} />
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

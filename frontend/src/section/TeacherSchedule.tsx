import {useState, useEffect} from "react";
import {
    Box,
    Container,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Typography,
    CircularProgress
} from "@mui/material";
import WeeklyCalendar from "../component/WeeklyCalendar";
import {TeacherScheduleResponse} from "../types/schedule";

// Mock data - replace with actual API call
const mockTeachers = [
    { id: 1, name: "John Smith" },
    { id: 2, name: "Dr. Johnson" },
    { id: 3, name: "Molly Hendrix" }
]

const mockTeacherSchedule: TeacherScheduleResponse = {
    teacherId: 1,
    teacherName: "John Smith",
    timeSlots: [
        {
            weekday: "MONDAY",
            start: 9,
            end: 10,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            classroom: "Classroom-205",
            classroomId: 205,
            filledSpots: 10,
            availableSpots: 0
        },
        {
            weekday: "WEDNESDAY",
            start: 10,
            end: 11,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            classroom: "Classroom-205",
            classroomId: 205,
            filledSpots: 10,
            availableSpots: 0
        },
        {
            weekday: "FRIDAY",
            start: 14,
            end: 16,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            classroom: "Classroom-205",
            classroomId: 205,
            filledSpots: 10,
            availableSpots: 0
        }
    ]
}

export default () => {
    const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(null)
    const [teacherSchedule, setTeacherSchedule] = useState<TeacherScheduleResponse | null>(null)
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (selectedTeacherId) {
            setLoading(true)
            // TODO: Replace with actual API call
            // fetchTeacherSchedule(selectedTeacherId)
            setTimeout(() => {
                setTeacherSchedule(mockTeacherSchedule)
                setLoading(false)
            }, 300)
        }
    }, [selectedTeacherId])

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
                            {mockTeachers.map(teacher => (
                                <MenuItem key={teacher.id} value={teacher.id}>
                                    {teacher.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                </Paper>

                {loading && (
                    <Box display="flex" justifyContent="center" p={4}>
                        <CircularProgress />
                    </Box>
                )}

                {!loading && teacherSchedule && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            {teacherSchedule.teacherName}'s Weekly Schedule
                        </Typography>
                        <WeeklyCalendar timeSlots={teacherSchedule.timeSlots} />
                    </Box>
                )}

                {!loading && !teacherSchedule && selectedTeacherId && (
                    <Box display="flex" justifyContent="center" p={4}>
                        <Typography variant="body1" color="text.secondary">
                            No schedule found for this teacher
                        </Typography>
                    </Box>
                )}
            </Box>
        </Container>
    )
}

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
import {StudentScheduleResponse} from "../types/schedule";

// Mock data - replace with actual API call
const mockStudents = [
    { id: 1, name: "Alice Johnson" },
    { id: 2, name: "Bob Smith" },
    { id: 3, name: "Carol Williams" }
]

const mockStudentSchedule: StudentScheduleResponse = {
    studentId: 1,
    studentName: "Alice Johnson",
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
            teacher: "John Smith",
            teacherId: 1
        },
        {
            weekday: "TUESDAY",
            start: 9,
            end: 11,
            courseCode: "SCI201",
            courseName: "Physics I",
            section: 1,
            classroom: "Science-Lab-A",
            classroomId: 301,
            teacher: "Dr. Johnson",
            teacherId: 2
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
            teacher: "John Smith",
            teacherId: 1
        },
        {
            weekday: "THURSDAY",
            start: 13,
            end: 15,
            courseCode: "SCI201",
            courseName: "Physics I",
            section: 1,
            classroom: "Science-Lab-A",
            classroomId: 301,
            teacher: "Dr. Johnson",
            teacherId: 2
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
            teacher: "John Smith",
            teacherId: 1
        }
    ]
}

export default () => {
    const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null)
    const [studentSchedule, setStudentSchedule] = useState<StudentScheduleResponse | null>(null)
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (selectedStudentId) {
            setLoading(true)
            // TODO: Replace with actual API call
            // fetchStudentSchedule(selectedStudentId)
            setTimeout(() => {
                setStudentSchedule(mockStudentSchedule)
                setLoading(false)
            }, 300)
        }
    }, [selectedStudentId])

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
                            {mockStudents.map(student => (
                                <MenuItem key={student.id} value={student.id}>
                                    {student.name}
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

                {!loading && studentSchedule && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            {studentSchedule.studentName}'s Weekly Schedule
                        </Typography>
                        <WeeklyCalendar timeSlots={studentSchedule.timeSlots} />
                    </Box>
                )}

                {!loading && !studentSchedule && selectedStudentId && (
                    <Box display="flex" justifyContent="center" p={4}>
                        <Typography variant="body1" color="text.secondary">
                            No schedule found for this student
                        </Typography>
                    </Box>
                )}
            </Box>
        </Container>
    )
}

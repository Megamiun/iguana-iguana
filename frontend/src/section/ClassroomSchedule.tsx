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
import {ClassroomScheduleResponse} from "../types/schedule";

// Mock data - replace with actual API call
const mockClassrooms = [
    { id: 205, name: "Classroom-205" },
    { id: 301, name: "Science-Lab-A" },
    { id: 102, name: "Art-Studio-1" }
]

const mockClassroomSchedule: ClassroomScheduleResponse = {
    classroomId: 205,
    classroomName: "Classroom-205",
    timeSlots: [
        {
            weekday: "MONDAY",
            start: 9,
            end: 10,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 10,
            availableSpots: 0
        },
        {
            weekday: "MONDAY",
            start: 10,
            end: 11,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 3,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 9,
            availableSpots: 1
        },
        {
            weekday: "WEDNESDAY",
            start: 10,
            end: 11,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 10,
            availableSpots: 0
        },
        {
            weekday: "WEDNESDAY",
            start: 11,
            end: 12,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 3,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 9,
            availableSpots: 1
        },
        {
            weekday: "FRIDAY",
            start: 12,
            end: 14,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 3,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 9,
            availableSpots: 1
        },
        {
            weekday: "FRIDAY",
            start: 14,
            end: 16,
            courseCode: "MAT101",
            courseName: "Algebra I",
            section: 1,
            teacher: "John Smith",
            teacherId: 1,
            filledSpots: 10,
            availableSpots: 0
        }
    ]
}

export default () => {
    const [selectedClassroomId, setSelectedClassroomId] = useState<number | null>(null)
    const [classroomSchedule, setClassroomSchedule] = useState<ClassroomScheduleResponse | null>(null)
    const [loading, setLoading] = useState(false)

    useEffect(() => {
        if (selectedClassroomId) {
            setLoading(true)
            // TODO: Replace with actual API call
            // fetchClassroomSchedule(selectedClassroomId)
            setTimeout(() => {
                setClassroomSchedule(mockClassroomSchedule)
                setLoading(false)
            }, 300)
        }
    }, [selectedClassroomId])

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
                            {mockClassrooms.map(classroom => (
                                <MenuItem key={classroom.id} value={classroom.id}>
                                    {classroom.name}
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

                {!loading && classroomSchedule && (
                    <Box>
                        <Typography variant="h6" gutterBottom>
                            {classroomSchedule.classroomName} Weekly Schedule
                        </Typography>
                        <WeeklyCalendar timeSlots={classroomSchedule.timeSlots} />
                    </Box>
                )}

                {!loading && !classroomSchedule && selectedClassroomId && (
                    <Box display="flex" justifyContent="center" p={4}>
                        <Typography variant="body1" color="text.secondary">
                            No schedule found for this classroom
                        </Typography>
                    </Box>
                )}
            </Box>
        </Container>
    )
}

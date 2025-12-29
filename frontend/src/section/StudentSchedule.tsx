import {useEffect, useState} from "react";
import {
    Alert,
    Box,
    Container,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Snackbar,
    Typography
} from "@mui/material";
import WeeklyCalendar from "../component/WeeklyCalendar";
import EnrollmentDialog from "./enrollment/EnrollmentDialog";
import {enrollStudent, getAvailableCourses, getStudents, getStudentSchedule} from "../service/apiClient";
import {PageResponse, Semester} from "../types/semester";
import {AvailableCourseSectionResponse, StudentScheduleResponse} from "../types/schedule";
import {StudentResponse} from "../types/base";
import AvailableCourses from "./enrollment/AvailableCourses";

interface GroupedCourse {
    courseCode: string;
    courseName: string;
    courseDescription?: string;
    credits: number;
    sections: AvailableCourseSectionResponse[];
}

export default () => {
    const [selectedStudentId, setSelectedStudentId] = useState<number | null>(null)
    const [studentSchedule, setStudentSchedule] = useState<StudentScheduleResponse | null>(null)
    const [availableCourses, setAvailableCourses] = useState<PageResponse<AvailableCourseSectionResponse> | null>(null)
    const [students, setStudents] = useState<StudentResponse[] | null>(null)
    const [selectedCourse, setSelectedCourse] = useState<GroupedCourse | null>(null)
    const [snackbar, setSnackbar] = useState<{open: boolean, message: string, severity: 'success' | 'error'}>({
        open: false, message: '', severity: 'success'
    })

    const loadStudents = async () => {
        setStudents((await getStudents()).content)
    }

    const loadStudentData = async (studentId: number) => {
        // TODO Remove hardcoded year and semester
        const year = 2024;
        const semester = Semester.FALL;

        setStudentSchedule(await getStudentSchedule(studentId, year, semester))
        setAvailableCourses(await getAvailableCourses(studentId, year, semester))
    }

    const handleOpenSectionModal = (course: GroupedCourse) => {
        setSelectedCourse(course);
    }

    const handleCloseSectionModal = () => {
        setSelectedCourse(null);
    }

    const handleEnroll = async (sectionId: number) => {
        if (!selectedStudentId) return;

        const response = await enrollStudent(selectedStudentId, sectionId);

        setSnackbar({ open: true, message: response.message, severity: response.success ? 'success' : 'error' });

        if (response.success) {
            handleCloseSectionModal();
            await loadStudentData(selectedStudentId);
        }
    }

    useEffect(() => { loadStudents() }, []);
    useEffect(() => { selectedStudentId && loadStudentData(selectedStudentId) }, [selectedStudentId])

    if (students == null)
        return <></>

    return <Container maxWidth="xl">
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
                                {student.fullName}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </Paper>

            {studentSchedule ? (
                <Box>
                    <Typography variant="h6" gutterBottom>
                        {studentSchedule.studentName} Weekly Schedule
                    </Typography>
                    <WeeklyCalendar timeSlots={studentSchedule.timeSlots} />
                </Box>
            ) : (
                <Box display="flex" justifyContent="center" p={4}>
                    <Typography variant="body1" color="text.secondary">
                        No schedule found for this student
                    </Typography>
                </Box>
            )}

            <AvailableCourses courses={availableCourses?.content} onEnrollClick={handleOpenSectionModal} />
        </Box>

        <EnrollmentDialog
            open={selectedCourse !== null}
            courseCode={selectedCourse?.courseCode}
            courseName={selectedCourse?.courseName}
            courseDescription={selectedCourse?.courseDescription}
            sections={selectedCourse?.sections}
            onClose={handleCloseSectionModal}
            onEnroll={handleEnroll}
        />

        <Snackbar
            open={snackbar.open}
            autoHideDuration={6000}
            onClose={() => setSnackbar({...snackbar, open: false})}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        >
            <Alert
                onClose={() => setSnackbar({...snackbar, open: false})}
                severity={snackbar.severity}
                sx={{ width: '100%' }}
            >
                {snackbar.message}
            </Alert>
        </Snackbar>
    </Container>
}

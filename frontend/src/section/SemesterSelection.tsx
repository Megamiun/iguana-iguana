import {PropsWithChildren, useEffect, useState} from "react";
import {
    Box,
    Button,
    CircularProgress,
    Container,
    FormControl,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Stack,
    Typography
} from "@mui/material";
import {SemesterResponse} from "../types/semester";
import {deleteSchedule, generateSchedule, getSchedule, getSemesters} from "../service/apiClient";
import {ScheduleResponse} from "../types/schedule";

const minWidth = 200

type SemesterSelectionProps = PropsWithChildren<{
    schedule?: ScheduleResponse
    setSchedule: (schedule: ScheduleResponse) => void
}>

export default ({schedule, setSchedule}: SemesterSelectionProps) => {
    const [availableSemesters, setAvailableSemesters] = useState<SemesterResponse[]>([])
    const [selectedSemesterId, setSelectedSemesterId] = useState<number | null>(null)
    const [loading, setLoading] = useState(true)

    const fetchSemesters = async () => {
        try {
            const response = await getSemesters()
            setAvailableSemesters(response.content)
            setSelectedSemesterId(response.content.find(semester => semester.isActive).id)
        } catch (error) {
            console.error("Failed to fetch semesters:", error)
        } finally {
            setLoading(false)
        }
    }

    const loadSchedule = async () => {
        if (!selectedSemester) return

        try {
            const schedule = await getSchedule(selectedSemester.semester, selectedSemester.year)
            setSchedule(schedule)
        } catch (error) {
            console.error("Failed to check/load schedule:", error)
            setSchedule(null)
        }
    }

    const handleGenerate = async () => {
        if (!selectedSemester) return

        try {
            const schedule = await generateSchedule(selectedSemester.semester, selectedSemester.year)
            setSchedule(schedule)
        } catch (error) {
            console.error("Failed to generate schedule:", error)
            setSchedule(null)
        }
    }

    const handleDelete = async () => {
        if (!selectedSemester) return

        try {
            await deleteSchedule(selectedSemester.semester, selectedSemester.year)
        } catch (error) {
            console.error("Failed to delete schedule:", error)
        }
        setSchedule(null)
    }

    useEffect(() => { fetchSemesters() }, [])
    useEffect(() => { loadSchedule() }, [selectedSemesterId])

    const selectedSemester = availableSemesters.find(s => s.id === selectedSemesterId)

    if (loading) {
        return <Container maxWidth="md">
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <CircularProgress />
            </Box>
        </Container>
    }

    if (availableSemesters.length === 0) {
        return <Container maxWidth="md">
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
                <Typography variant="h6" color="text.secondary">
                    No semesters available for scheduling
                </Typography>
            </Box>
        </Container>
    }

    return <Box sx={{ margin: 4 }} maxWidth="md">
        <Paper elevation={3} sx={{ p: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom>
                Master Schedule Generator
            </Typography>
            <Typography variant="body1" color="text.secondary" paragraph>
                Select a semester to generate the master schedule
            </Typography>

            <Stack direction="row" spacing={2} alignItems="center" sx={{ mt: 3 }}>
                <FormControl sx={{minWidth: minWidth}}>
                    <InputLabel id="semester-select-label">Semester</InputLabel>
                    <Select
                        variant="outlined"
                        label="Semester"
                        labelId="semester-select-label"
                        value={selectedSemesterId ?? ''}
                        onChange={e => setSelectedSemesterId(Number(e.target.value))}>
                        {
                            availableSemesters.map(semester =>
                                <MenuItem key={semester.id} value={semester.id}>
                                    {semester.name} {semester.year}
                                </MenuItem>
                            )
                        }
                    </Select>
                </FormControl>
                {schedule != undefined ? (
                    <Button
                        variant="outlined"
                        color="error"
                        size="large"
                        onClick={handleDelete}
                        disabled={!selectedSemester || !selectedSemester.isActive}
                    >Delete</Button>
                ) : (
                    <Button
                        variant="contained"
                        size="large"
                        onClick={handleGenerate}
                        disabled={!selectedSemester || !selectedSemester.isActive}
                    >Generate</Button>
                )}
            </Stack>
        </Paper>
    </Box>
}

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
import {generateSchedule, getSemesters} from "../service/apiClient";
import {ScheduleResponse} from "../types/schedule";

const minWidth = 200

type SemesterSelectionProps = PropsWithChildren<{
    setSchedule: (schedule: ScheduleResponse) => void
}>

export default ({setSchedule}: SemesterSelectionProps) => {
    const [availableSemesters, setAvailableSemesters] = useState<SemesterResponse[]>([])
    const [selectedSemesterId, setSelectedSemesterId] = useState<number | null>(null)
    const [loading, setLoading] = useState(true)

    const fetchSemesters = async () => {
        try {
            const response = await getSemesters()
            setAvailableSemesters(response.content)
            if (response.content.length > 0) {
                setSelectedSemesterId(response.content[0].id)
            }
        } catch (error) {
            console.error("Failed to fetch semesters:", error)
        } finally {
            setLoading(false)
        }
    }

    const handleGenerate = async () => {
        if (!selectedSemester) return

        setSchedule(await generateSchedule(selectedSemester.semester, selectedSemester.year))
    }

    useEffect(() => { fetchSemesters() }, [])

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
                <Button
                    variant="contained"
                    size="large"
                    onClick={handleGenerate}
                    disabled={!selectedSemester}
                >Generate</Button>
            </Stack>
        </Paper>
    </Box>
}

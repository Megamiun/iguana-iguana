import {Box, Card, CardContent, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography} from "@mui/material";
import {TimeSlot} from "../types/schedule";

interface WeeklyCalendarProps {
    timeSlots: TimeSlot[]
}

const WEEKDAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY']
const TIME_SLOTS = [9, 10, 11, 12, 13, 14, 15, 16]

const formatTime = (hour: number): string => {
    if (hour === 12) return "12PM";
    if (hour > 12) return `${hour - 12}PM`;
    return `${hour}AM`;
}

const formatTimeRange = (start: number, end: number): string => {
    return `${formatTime(start)}-${formatTime(end)}`;
}

export default ({timeSlots}: WeeklyCalendarProps) => {
    // Group time slots by day and hour
    const slotsByDayAndTime = new Map<string, TimeSlot>()

    timeSlots.forEach(slot => {
        const key = `${slot.weekday.toUpperCase()}-${slot.start}`
        slotsByDayAndTime.set(key, slot)
    })

    const getSlotForDayAndTime = (day: string, hour: number): TimeSlot | null => {
        return slotsByDayAndTime.get(`${day}-${hour}`) || null
    }

    return (
        <TableContainer component={Paper}>
            <Table sx={{ minWidth: 650 }} size="small">
                <TableHead>
                    <TableRow>
                        <TableCell sx={{ fontWeight: 'bold', width: '100px' }}>Time</TableCell>
                        {WEEKDAYS.map(day => (
                            <TableCell key={day} align="center" sx={{ fontWeight: 'bold' }}>
                                {day.charAt(0) + day.slice(1).toLowerCase()}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>
                    {TIME_SLOTS.map(hour => {
                        if (hour === 12) {
                            return (
                                <TableRow key="lunch">
                                    <TableCell sx={{ fontWeight: 'bold' }}>12-1PM</TableCell>
                                    <TableCell colSpan={5} align="center" sx={{ backgroundColor: '#f5f5f5' }}>
                                        <Typography variant="body2" color="text.secondary">
                                            LUNCH BREAK
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            )
                        }

                        return (
                            <TableRow key={hour}>
                                <TableCell sx={{ fontWeight: 'bold' }}>
                                    {formatTimeRange(hour, hour + 1)}
                                </TableCell>
                                {WEEKDAYS.map(day => {
                                    const slot = getSlotForDayAndTime(day, hour)

                                    return (
                                        <TableCell key={`${day}-${hour}`} sx={{ p: 0.5, verticalAlign: 'top' }}>
                                            {slot && (
                                                <Card variant="outlined" sx={{ height: '100%', minHeight: '80px' }}>
                                                    <CardContent sx={{ p: 1, '&:last-child': { pb: 1 } }}>
                                                        <Typography variant="body2" fontWeight="bold">
                                                            {slot.courseCode}
                                                        </Typography>
                                                        {slot.section && (
                                                            <Typography variant="caption" display="block" color="text.secondary">
                                                                Section {slot.section}
                                                            </Typography>
                                                        )}
                                                        {slot.classroom && (
                                                            <Typography variant="caption" display="block">
                                                                {slot.classroom}
                                                            </Typography>
                                                        )}
                                                        {slot.teacher && (
                                                            <Typography variant="caption" display="block">
                                                                {slot.teacher}
                                                            </Typography>
                                                        )}
                                                        {slot.start !== hour && (
                                                            <Typography variant="caption" display="block" color="primary">
                                                                {formatTimeRange(slot.start, slot.end)}
                                                            </Typography>
                                                        )}
                                                    </CardContent>
                                                </Card>
                                            )}
                                        </TableCell>
                                    )
                                })}
                            </TableRow>
                        )
                    })}
                </TableBody>
            </Table>
        </TableContainer>
    )
}
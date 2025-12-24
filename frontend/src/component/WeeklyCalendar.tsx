import {
    Card,
    CardContent,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography
} from "@mui/material";
import {TimeSlot} from "../types/schedule";
import {PropsWithChildren} from "react";

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

const range = (start: number, end: number) =>
    Array.from({length: (end - start)}, (_, key) => start + key)

export default ({timeSlots}: WeeklyCalendarProps) => {
    const slotsByDayAndTime = {}

    timeSlots.forEach(slot => {
        range(slot.start, slot.end).forEach(hour => {
            slotsByDayAndTime[`${slot.weekday.toUpperCase()}-${hour}`] = slot
        })
    })

    return (
        <TableContainer component={Paper}>
            <Table sx={{minWidth: 650}} size="small">
                <TableHead>
                    <TableRow>
                        <TableCell sx={{fontWeight: 'bold', width: '100px'}}>Time</TableCell>
                        {WEEKDAYS.map(day => (
                            <TableCell key={day} align="center" sx={{fontWeight: 'bold'}}>
                                {day.charAt(0) + day.slice(1).toLowerCase()}
                            </TableCell>
                        ))}
                    </TableRow>
                </TableHead>
                <TableBody>{
                    TIME_SLOTS.map(hour =>
                        <TableRow key={hour}>
                            <TableCell sx={{fontWeight: 'bold'}}>
                                {formatTimeRange(hour, hour + 1)}
                            </TableCell>
                            {WEEKDAYS.map(day =>
                                <CalendarCell key={`${day}-${hour}`} slot={slotsByDayAndTime[`${day}-${hour}`]}/>
                            )}
                        </TableRow>
                    )
                }</TableBody>
            </Table>
        </TableContainer>
    )
}

const CalendarCell = ({slot}: PropsWithChildren<({ slot: TimeSlot })>) => {
    if (slot == null)
        return <TableCell sx={{p: 0.5, verticalAlign: 'top'}}></TableCell>

    return <TableCell sx={{p: 0.5, verticalAlign: 'top'}}>
        <Card variant="outlined" sx={{height: '100%', minHeight: '80px'}}>
            <CardContent sx={{p: 1, '&:last-child': {pb: 1}}}>
                <Typography variant="body2" fontWeight="bold">{slot.courseCode}</Typography>
                <Typography variant="caption" display="block" color="text.secondary">Section {slot.section}</Typography>
                <Typography variant="caption" display="block">{slot.classroom}</Typography>
                <Typography variant="caption" display="block">{slot.teacher}</Typography>
                <Typography variant="caption" display="block" color="primary">{formatTimeRange(slot.start, slot.end)}</Typography>
            </CardContent>
        </Card>
    </TableCell>
}
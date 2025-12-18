import {PropsWithChildren} from "react";
import {CourseResponse, ScheduleDurationResponse, ScheduleResponse} from "../types/schedule";
import {Box, Card, CardContent} from "@mui/material";

type ScheduleDisplayProps = PropsWithChildren<{
    schedule?: ScheduleResponse
}>

const formatTime = (hour: number): string => {
    if (hour === 12) return "12PM";
    if (hour > 12) return `${hour - 12}PM`;
    return `${hour}AM`;
}

const formatSchedule = (schedule: ScheduleDurationResponse[]): string => {
    return schedule.map(slot =>
        `${slot.weekday} ${formatTime(slot.start)}-${formatTime(slot.end)}`
    ).join(", ");
}

const formatStudentCount = (course: CourseResponse): string => {
    if (course.availableSpots === 0) {
        return `${course.filledSpots} (capacity full)`;
    }
    return `${course.filledSpots} (${course.availableSpots} spot${course.availableSpots > 1 ? 's' : ''} available)`;
}

export default ({schedule}: ScheduleDisplayProps) => {
    if (schedule == null)
        return <></>

    return <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, p: 2 }}>{
        schedule.courses.map((course, index) => (
            <Card className="course-card" key={index} variant="outlined">
                <CardContent>
                    <p><strong>Course:</strong> {course.name} / Section - {course.section}</p>
                    <p><strong>Teacher:</strong> {course.teacher}</p>
                    <p><strong>Room:</strong> {course.classroom}</p>
                    <p><strong>Schedule:</strong> {formatSchedule(course.schedule)}</p>
                    <p><strong>Students:</strong> {formatStudentCount(course)}</p>
                </CardContent>
            </Card>
        ))
    }</Box>
}

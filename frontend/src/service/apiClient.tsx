import {Semester} from "../types/semester";
import {ScheduleResponse} from "../types/schedule";

export const generateSchedule = (semester: Semester, year: number) => {
    console.log(`Generating schedule for the ${semester} of ${year}`)

    return {
        courses: [
            {
                name: "MAT101",
                section: "1",
                teacher: "John Smith",
                classroom: "Classroom-205",
                schedule: [
                    { weekday: "Monday", start: 9, end: 10 },
                    { weekday: "Wednesday", start: 10, end: 11 },
                    { weekday: "Friday", start: 14, end: 16 }
                ],
                availableSpots: 0,
                filledSpots: 10
            },
            {
                name: "MAT101",
                section: "2",
                teacher: "Molly Hendrix",
                classroom: "Classroom-204",
                schedule: [
                    { weekday: "Monday", start: 9, end: 10 },
                    { weekday: "Wednesday", start: 10, end: 11 },
                    { weekday: "Friday", start: 14, end: 16 }
                ],
                availableSpots: 5,
                filledSpots: 5
            },
            {
                name: "MAT101",
                section: "3",
                teacher: "John Smith",
                classroom: "Classroom-205",
                schedule: [
                    { weekday: "Monday", start: 10, end: 11 },
                    { weekday: "Wednesday", start: 11, end: 12 },
                    { weekday: "Friday", start: 12, end: 14 }
                ],
                availableSpots: 1,
                filledSpots: 9
            },
            {
                name: "SCI201",
                section: "1",
                teacher: "Dr. Johnson",
                classroom: "Science-Lab-A",
                schedule: [
                    { weekday: "Tuesday", start: 9, end: 11 },
                    { weekday: "Thursday", start: 13, end: 15 }
                ],
                availableSpots: 2,
                filledSpots: 8
            }
        ]
    } as ScheduleResponse
}

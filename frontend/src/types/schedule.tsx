import {Semester} from "./semester";

export interface ScheduleDurationResponse {
    weekday: string,
    start: number,
    end: number
}

export interface CourseResponse {
    name: string,
    section: string,
    teacher: string,
    classroom: string
    schedule: ScheduleDurationResponse[],
    availableSpots: number,
    filledSpots: number
}

export interface ScheduleResponse {
    courses: CourseResponse[]
}

export interface ScheduleGenerationRequest {
    semester: Semester,
    year: number
}
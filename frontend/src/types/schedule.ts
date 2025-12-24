export interface ScheduleDurationResponse {
    weekday: string,
    start: number,
    end: number
}

export interface CourseResponse {
    code: string,
    name: string,
    section: number,
    teacher: string,
    teacherId: number,
    classroom: string,
    classroomId: number,
    schedule: ScheduleDurationResponse[],
    availableSpots: number,
    filledSpots: number
}

export interface ScheduleResponse {
    courses: CourseResponse[]
}

// Types for individual schedule views
export interface TimeSlot {
    weekday: string,
    start: number,
    end: number,
    courseCode: string,
    courseName: string,
    section: number,
    classroom?: string,
    classroomId?: number,
    teacher?: string,
    teacherId?: number,
    filledSpots?: number,
    availableSpots?: number
}

export interface TeacherScheduleResponse {
    teacherId: number,
    teacherName: string,
    timeSlots: TimeSlot[]
}

export interface StudentScheduleResponse {
    studentId: number,
    studentName: string,
    timeSlots: TimeSlot[]
}

export interface ClassroomScheduleResponse {
    classroomId: number,
    classroomName: string,
    timeSlots: TimeSlot[]
}

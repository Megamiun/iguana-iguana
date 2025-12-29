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

export enum UnavailabilityReason {
    NO_SPOTS = 'NO_SPOTS',
    TIME_CONFLICT = 'TIME_CONFLICT',
    ALREADY_ENROLLED = 'ALREADY_ENROLLED',
    PREREQUISITE_NOT_MET = 'PREREQUISITE_NOT_MET',
    GRADE_LEVEL_REQUIREMENT = 'GRADE_LEVEL_REQUIREMENT',
    MAX_ENROLLMENTS_REACHED = 'MAX_ENROLLMENTS_REACHED'
}

export interface AvailableCourseSectionResponse {
    sectionId: number,
    courseCode: string,
    courseName: string,
    courseDescription?: string,
    credits: number,
    section: number,
    teacher: string,
    classroom: string,
    schedule: ScheduleDurationResponse[],
    availableSpots: number,
    filledSpots: number,
    available: boolean,
    unavailableReason?: UnavailabilityReason
}

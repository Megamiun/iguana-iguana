export interface TeacherResponse {
    id: number;
    firstName: string;
    lastName: string;
    fullName: string;
    email: string;
    specializationName: string | null;
    maxDailyHours: number;
}

export interface ClassroomResponse {
    id: number;
    name: string;
    roomTypeName: string | null;
    equipment: string;
    capacity: number;
}

export interface StudentResponse {
    id: number;
    firstName: string;
    lastName: string;
    fullName: string;
    email: string;
    gradeLevel: number;
    enrollmentYear: number;
    expectedGraduationYear: number;
    status: string;
}

export interface Result {
    success: boolean,
    message?: string
}

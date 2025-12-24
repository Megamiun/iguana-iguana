import {PageResponse, Semester, SemesterResponse} from "../types/semester";
import {
    ClassroomScheduleResponse,
    ScheduleResponse,
    StudentScheduleResponse,
    TeacherScheduleResponse
} from "../types/schedule";
import {ClassroomResponse, StudentResponse, TeacherResponse} from "../types/base";

const API_BASE_URL = import.meta.env.VITE_MAPLEWOOD_BASE_URL || "/api";

// For simplicity’s sake, setting this as the default
const DEFAULT_PAGE_SIZE = 2147483647;

// Semesters
export const getSemesters = async (): Promise<PageResponse<SemesterResponse>> => {
    const response = await fetch(`${API_BASE_URL}/semesters`);

    if (!response.ok) {
        throw new Error(`Failed to fetch semesters: ${response.statusText}`);
    }

    return await response.json();
};

export const generateSchedule = async (semester: Semester, year: number): Promise<ScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/schedule/${year}/${semester}`, { method: "POST" });

    if (!response.ok) {
        throw new Error(`Failed to generate schedule: ${response.statusText}`);
    }

    return await response.json();
}

export const getSchedule = async (semester: Semester, year: number): Promise<ScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/schedule/${year}/${semester}`, { method: "GET" });

    if (response.status == 404) {
        return null;
    }

    if (!response.ok) {
        throw new Error(`Failed to retrieve schedule: ${response.statusText}`);
    }

    return await response.json();
}

export const deleteSchedule = async (semester: Semester, year: number): Promise<void> => {
    const response = await fetch(`${API_BASE_URL}/schedule/${year}/${semester}`, { method: "DELETE" });

    if (!response.ok) {
        throw new Error(`Failed to delete schedule: ${response.statusText}`);
    }
}

// Students
export const getStudents = async (page: number = 0, size: number = DEFAULT_PAGE_SIZE): Promise<PageResponse<StudentResponse>> => {
    const response = await fetch(`${API_BASE_URL}/students?page=${page}&size=${size}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch students: ${response.statusText}`);
    }

    return await response.json();
};

export const getStudentSchedule = async (id: number, year: number, semester: Semester): Promise<StudentScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/students/${id}/schedule/${year}/${semester}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch student schedule: ${response.statusText}`);
    }

    return await response.json();
};

// Teachers
export const getTeachers = async (page: number = 0, size: number = DEFAULT_PAGE_SIZE): Promise<PageResponse<TeacherResponse>> => {
    const response = await fetch(`${API_BASE_URL}/teachers?page=${page}&size=${size}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch teachers: ${response.statusText}`);
    }

    return await response.json();
};

export const getTeacherSchedule = async (id: number, year: number, semester: Semester): Promise<TeacherScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/teachers/${id}/schedule/${year}/${semester}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch teacher schedule: ${response.statusText}`);
    }

    return await response.json();
};

// Classrooms
export const getClassrooms = async (page: number = 0, size: number = DEFAULT_PAGE_SIZE): Promise<PageResponse<ClassroomResponse>> => {
    const response = await fetch(`${API_BASE_URL}/classrooms?page=${page}&size=${size}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch classrooms: ${response.statusText}`);
    }

    return await response.json();
};

export const getClassroomSchedule = async (id: number, year: number, semester: Semester): Promise<ClassroomScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/classrooms/${id}/schedule/${year}/${semester}`);

    if (!response.ok) {
        throw new Error(`Failed to fetch classroom schedule: ${response.statusText}`);
    }

    return await response.json();
};

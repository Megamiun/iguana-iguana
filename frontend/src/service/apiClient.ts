import {Semester, SemesterResponse, PageResponse} from "../types/semester";
import {ScheduleGenerationRequest, ScheduleResponse} from "../types/schedule";

const API_BASE_URL = import.meta.env.VITE_MAPLEWOOD_BASE_URL || "/api";

export const getSemesters = async (): Promise<PageResponse<SemesterResponse>> => {
    const response = await fetch(`${API_BASE_URL}/schedule/semesters`);

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

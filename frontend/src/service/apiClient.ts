import {Semester} from "../types/semester";
import {ScheduleGenerationRequest, ScheduleResponse} from "../types/schedule";

const API_BASE_URL = import.meta.env.VITE_MAPLEWOOD_BASE_URL || "/api";

export const generateSchedule = async (semester: Semester, year: number): Promise<ScheduleResponse> => {
    const response = await fetch(`${API_BASE_URL}/schedule`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ semester, year } as ScheduleGenerationRequest),
    });

    if (!response.ok) {
        throw new Error(`Failed to generate schedule: ${response.statusText}`);
    }

    return await response.json();
}

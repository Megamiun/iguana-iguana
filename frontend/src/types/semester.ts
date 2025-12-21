export enum Semester { FALL = 'FALL', SPRING = 'SPRING' }

export interface SemesterResponse {
    id: number;
    name: string;
    year: number;
    semester: Semester;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

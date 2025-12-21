export enum Semester { FALL = 'FALL', SPRING = 'SPRING' }

export const getSemesterDisplay = (semester: Semester): string => {
    return semester.charAt(0) + semester.slice(1).toLowerCase()
}

import {UnavailabilityReason} from "../../types/schedule";

export const getUnavailabilityMessage = (reason?: UnavailabilityReason): string => {
    switch (reason) {
        case UnavailabilityReason.NO_SPOTS:
            return 'Section is full';
        case UnavailabilityReason.TIME_CONFLICT:
            return 'Time conflict with your schedule';
        case UnavailabilityReason.ALREADY_ENROLLED:
            return 'Already enrolled in this course';
        case UnavailabilityReason.PREREQUISITE_NOT_MET:
            return 'Prerequisites not met';
        case UnavailabilityReason.GRADE_LEVEL_REQUIREMENT:
            return 'Grade level requirement not met';
        case UnavailabilityReason.MAX_ENROLLMENTS_REACHED:
            return 'Maximum of enrollments per semester reached';
        default:
            return 'Not available';
    }
}

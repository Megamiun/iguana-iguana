import {Paper, Stack, Typography} from "@mui/material";
import CourseCard from "./CourseCard";
import {AvailableCourseSectionResponse, UnavailabilityReason} from "../../types/schedule";
import {getUnavailabilityMessage} from "./utils";

interface GroupedCourse {
    courseCode: string;
    courseName: string;
    courseDescription?: string;
    credits: number;
    sections: AvailableCourseSectionResponse[];
}

interface AvailableCoursesProps {
    courses?: AvailableCourseSectionResponse[];
    onEnrollClick: (course: GroupedCourse) => void;
}

const getCourseWarning = (course: GroupedCourse): string | null => {
    const hasMaxEnrollmentsReached = course.sections.some(
        section => section.unavailableReason === UnavailabilityReason.MAX_ENROLLMENTS_REACHED
    );

    const hasAlreadyEnrolled = course.sections.some(
        section => section.unavailableReason === UnavailabilityReason.ALREADY_ENROLLED
    );

    if (hasMaxEnrollmentsReached) {
        return getUnavailabilityMessage(UnavailabilityReason.MAX_ENROLLMENTS_REACHED);
    }

    if (hasAlreadyEnrolled) {
        return getUnavailabilityMessage(UnavailabilityReason.ALREADY_ENROLLED);
    }

    return null;
}

const groupCoursesByCourseCode = (availableCourses?: AvailableCourseSectionResponse[]): GroupedCourse[] => {
    if (!availableCourses) return [];

    const grouped = new Map<string, GroupedCourse>();

    availableCourses.forEach(section => {
        if (!grouped.has(section.courseCode)) {
            grouped.set(section.courseCode, {
                courseCode: section.courseCode,
                courseName: section.courseName,
                courseDescription: section.courseDescription,
                credits: section.credits,
                sections: []
            });
        }
        grouped.get(section.courseCode)!.sections.push(section);
    });

    return Array.from(grouped.values());
}

export default ({ courses, onEnrollClick }: AvailableCoursesProps) => {
    if (courses == null || courses.length === 0) {
        return <Paper elevation={3} sx={{ p: 3, mt: 3 }}>
            <Typography variant="body1" color="text.secondary" align="center">
                No available courses to enroll in at this time.
            </Typography>
        </Paper>;
    }

    return <Paper elevation={3} sx={{ p: 3, mt: 3 }}>
        <Typography variant="h6" gutterBottom>
            Available Courses
        </Typography>
        <Typography variant="body2" color="text.secondary" component="p">
            Select a course to view and enroll in available sections
        </Typography>
        <Stack spacing={2}>
            {groupCoursesByCourseCode(courses).map((course) => (
                <CourseCard
                    key={course.courseCode}
                    courseCode={course.courseCode}
                    courseName={course.courseName}
                    courseDescription={course.courseDescription}
                    credits={course.credits}
                    sectionsCount={course.sections.length}
                    warning={getCourseWarning(course)}
                    onEnrollClick={() => onEnrollClick(course)}
                />
            ))}
        </Stack>
    </Paper>;
}

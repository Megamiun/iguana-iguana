package br.com.gabryel.maplewood.service;

import br.com.gabryel.maplewood.api.model.AvailableCourseSectionResponse;
import br.com.gabryel.maplewood.api.model.EligiblePageResponse;
import br.com.gabryel.maplewood.api.model.ScheduleDurationResponse;
import br.com.gabryel.maplewood.api.model.StudentResponse;
import br.com.gabryel.maplewood.api.model.StudentScheduleResponse;
import br.com.gabryel.maplewood.api.model.UnavailabilityReason;
import br.com.gabryel.maplewood.api.model.Weekday;
import br.com.gabryel.maplewood.config.EnrollmentConfig;
import br.com.gabryel.maplewood.exception.AlreadyEnrolledException;
import br.com.gabryel.maplewood.exception.GradeLevelRequirementException;
import br.com.gabryel.maplewood.exception.MaxEnrollmentsReachedException;
import br.com.gabryel.maplewood.exception.NoAvailableSpotsException;
import br.com.gabryel.maplewood.exception.PrerequisiteNotMetException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.exception.TimeConflictException;
import br.com.gabryel.maplewood.mapper.ResponseMapper;
import br.com.gabryel.maplewood.mapper.ScheduleMapper;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.CourseSection;
import br.com.gabryel.maplewood.model.db.CourseSectionStudent;
import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.enums.CourseHistoryStatus;
import br.com.gabryel.maplewood.model.dto.WeekdayTimeRange;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import br.com.gabryel.maplewood.repository.StudentCourseHistoryRepository;
import br.com.gabryel.maplewood.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static br.com.gabryel.maplewood.api.model.UnavailabilityReason.ALREADY_ENROLLED;
import static br.com.gabryel.maplewood.api.model.UnavailabilityReason.MAX_ENROLLMENTS_REACHED;
import static br.com.gabryel.maplewood.api.model.UnavailabilityReason.NO_SPOTS;
import static br.com.gabryel.maplewood.api.model.UnavailabilityReason.TIME_CONFLICT;
import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final StudentCourseHistoryRepository studentCourseHistoryRepository;
    private final SemesterRepository semesterRepository;
    private final ResponseMapper responseMapper;
    private final ScheduleMapper scheduleMapper;
    private final EnrollmentConfig enrollmentConfig;

    @Transactional(readOnly = true)
    public Page<StudentResponse> getStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(responseMapper::toStudentResponse);
    }

    @Transactional(readOnly = true)
    public StudentScheduleResponse getStudentSchedule(Integer studentId, Integer year, SemesterType semesterType) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var enrollments = courseSectionStudentRepository.findByStudentIdAndSemesterId(studentId, semester.getId());

        var timeSlots = enrollments.stream()
            .flatMap(enrollment -> scheduleMapper.toTimeSlotResponses(enrollment.getCourseSection()))
            .toList();

        return new StudentScheduleResponse()
            .studentId(student.getId())
            .studentName(responseMapper.formatFullName(student.getFirstName(), student.getLastName()))
            .timeSlots(timeSlots);
    }

    public EligiblePageResponse getAvailableCourses(Integer studentId, Integer year, SemesterType semesterType) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        var semester = semesterRepository.findByYearAndOrderInYear(year, getOrderInYear(semesterType))
            .orElseThrow(() -> new SemesterNotFoundException(year, semesterType.name()));

        var allSections = courseSectionRepository.findBySemesterId(semester.getId());
        var passedCourseIds = getPassedCourseIds(student);
        var currentEnrollments = courseSectionStudentRepository.findByStudentIdAndSemesterId(studentId, semester.getId());
        var occupiedTimeRanges = getOccupiedTimeRanges(currentEnrollments);

        var eligibleSections = allSections.stream()
            .filter(section -> meetsGradeLevel(section, student))
            .filter(section -> meetsPrerequisites(section, passedCourseIds))
            .map(section -> toAvailableCourseResponse(section, currentEnrollments, occupiedTimeRanges))
            .toList();

        return new EligiblePageResponse().content(eligibleSections);
    }

    @Transactional
    public void enrollStudent(Integer studentId, Integer sectionId) {
        var student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        var section = courseSectionRepository.findById(sectionId.longValue())
            .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));

        validateEnrollment(student, section);

        var enrollment = CourseSectionStudent.builder()
            .student(student)
            .courseSection(section)
            .build();

        courseSectionStudentRepository.save(enrollment);
    }

    private void validateEnrollment(Student student, CourseSection section) {
        if (hasNoAvailableSpots(section))
            throw new NoAvailableSpotsException();

        if (!meetsGradeLevel(section, student))
            throw new GradeLevelRequirementException();

        var passedCourseIds = getPassedCourseIds(student);
        if (!meetsPrerequisites(section, passedCourseIds))
            throw new PrerequisiteNotMetException();

        var currentEnrollments = courseSectionStudentRepository
            .findByStudentIdAndSemesterId(student.getId(), section.getSemester().getId());

        if (currentEnrollments.size() >= enrollmentConfig.getMaxPerSemester())
            throw new MaxEnrollmentsReachedException(enrollmentConfig.getMaxPerSemester());

        if (isAlreadyEnrolled(section, currentEnrollments))
            throw new AlreadyEnrolledException();

        var occupiedTimeRanges = getOccupiedTimeRanges(currentEnrollments);

        if (hasTimeConflict(section, occupiedTimeRanges))
            throw new TimeConflictException();
    }

    private static boolean isAlreadyEnrolled(CourseSection section, List<CourseSectionStudent> currentEnrollments) {
        return currentEnrollments.stream().anyMatch(enrollment ->
            enrollment.getCourseSection().getCourse().getId().equals(section.getCourse().getId()));
    }

    private boolean hasNoAvailableSpots(CourseSection section) {
        var enrollments = courseSectionStudentRepository.findByCourseSectionId(section.getId());
        return enrollments.size() >= section.getClassroom().getCapacity();
    }

    private boolean meetsGradeLevel(CourseSection section, Student student) {
        var course = section.getCourse();
        return student.getGradeLevel() >= course.getGradeLevelMin()
            && student.getGradeLevel() <= course.getGradeLevelMax();
    }

    private boolean meetsPrerequisites(CourseSection section, Set<Integer> passedCourseIds) {
        var prerequisite = section.getCourse().getPrerequisite();
        return prerequisite == null || passedCourseIds.contains(prerequisite.getId());
    }

    private boolean hasTimeConflict(CourseSection section, Set<WeekdayTimeRange> occupiedTimeSlots) {
        var sectionTimeSlots = courseSectionTimeSlotRepository.findByCourseSectionId(section.getId());
        return sectionTimeSlots.stream()
            .map(slot -> new WeekdayTimeRange(slot.getWeekday(), slot.getStartHour(), slot.getEndHour()))
            .anyMatch(courseSlot -> occupiedTimeSlots.stream().anyMatch(occupied -> occupied.intersects(courseSlot)));
    }

    private Set<WeekdayTimeRange> getOccupiedTimeRanges(List<CourseSectionStudent> enrollments) {
        return enrollments.stream().flatMap(enrollment ->
            courseSectionTimeSlotRepository
                .findByCourseSectionId(enrollment.getCourseSection().getId())
                .stream()
        ).map(slot -> new WeekdayTimeRange(slot.getWeekday(), slot.getStartHour(), slot.getEndHour()))
        .collect(toSet());
    }

    private Set<Integer> getPassedCourseIds(Student student) {
        var studentHistory = studentCourseHistoryRepository.findByStudentIn(List.of(student));
        return studentHistory.stream()
            .filter(history -> history.getStatus() == CourseHistoryStatus.PASSED)
            .map(history -> history.getCourse().getId())
            .collect(toSet());
    }

    private AvailableCourseSectionResponse toAvailableCourseResponse(
        CourseSection section,
        List<CourseSectionStudent> currentEnrollments,
        Set<WeekdayTimeRange> occupiedTimeRanges
    ) {
        var timeSlots = courseSectionTimeSlotRepository.findByCourseSectionId(section.getId());
        var enrollments = courseSectionStudentRepository.findByCourseSectionId(section.getId());
        var schedule = timeSlots.stream().map(slot ->
            new ScheduleDurationResponse()
                .weekday(Weekday.valueOf(slot.getWeekday().name()))
                .start(slot.getStartHour())
                .end(slot.getEndHour()))
        .toList();

        var unavailableReason = getUnavailabilityReason(section, currentEnrollments, occupiedTimeRanges);

        return new AvailableCourseSectionResponse()
            .sectionId(section.getId().intValue())
            .courseCode(section.getCourse().getCode())
            .courseName(section.getCourse().getName())
            .courseDescription(section.getCourse().getDescription())
            .credits(section.getCourse().getCredits())
            .section(section.getSectionNumber())
            .teacher(section.getTeacher().getFirstName() + " " + section.getTeacher().getLastName())
            .classroom(section.getClassroom().getName())
            .schedule(schedule)
            .filledSpots(enrollments.size())
            .availableSpots(section.getClassroom().getCapacity() - enrollments.size())
            .unavailableReason(unavailableReason)
            .available(unavailableReason == null);
    }

    private UnavailabilityReason getUnavailabilityReason(CourseSection section, List<CourseSectionStudent> currentEnrollments, Set<WeekdayTimeRange> occupiedTimeRanges) {
        if (isAlreadyEnrolled(section, currentEnrollments)) {
            return ALREADY_ENROLLED;
        } else if (currentEnrollments.size() >= enrollmentConfig.getMaxPerSemester()) {
            return MAX_ENROLLMENTS_REACHED;
        } else if (hasNoAvailableSpots(section)) {
            return NO_SPOTS;
        } else if (hasTimeConflict(section, occupiedTimeRanges)) {
            return TIME_CONFLICT;
        } else {
            return null;
        }
    }

    private static int getOrderInYear(SemesterType semesterType) {
        return semesterType == FALL ? 1 : 2;
    }
}

package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.exception.ScheduleAlreadyExistsException;
import br.com.gabryel.maplewood.exception.SemesterNotFoundException;
import br.com.gabryel.maplewood.model.SemesterType;
import br.com.gabryel.maplewood.model.db.Classroom;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.db.CourseSection;
import br.com.gabryel.maplewood.model.db.CourseSectionStudent;
import br.com.gabryel.maplewood.model.db.CourseSectionTimeSlot;
import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.Teacher;
import br.com.gabryel.maplewood.model.dto.ClassroomData;
import br.com.gabryel.maplewood.model.dto.CourseData;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.model.dto.StudentData;
import br.com.gabryel.maplewood.model.dto.TeacherData;
import br.com.gabryel.maplewood.model.dto.TimeRange;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static br.com.gabryel.maplewood.model.SemesterType.FALL;
import static br.com.gabryel.maplewood.model.SemesterType.SPRING;

@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {
    private final CourseSectionRepository courseSectionRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final SemesterRepository semesterRepository;
    private final CourseDataService courseDataService;
    private final TeacherDataService teacherDataService;
    private final ClassroomDataService classroomDataService;
    private final StudentDataService studentDataService;

    public boolean hasSchedule(Integer semesterId) {
        return courseSectionRepository.existsBySemesterId(semesterId);
    }

    public List<CourseSectionDto> findSchedule(Integer semesterId) {
        var semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new SemesterNotFoundException(semesterId));

        var semesterType = getSemesterType(semester.getOrderInYear());
        var courses = courseDataService.getCoursesFor(semesterType);
        var teachers = teacherDataService.getTeachers();
        var classrooms = classroomDataService.getClassrooms();
        var students = studentDataService.getStudents();

        var courseSections = courseSectionRepository.findBySemesterId(semesterId);

        return courseSections.stream()
            .map(section -> toDto(section, courses, teachers, classrooms, students))
            .toList();
    }

    @Transactional
    public void persistSchedule(Integer semesterId, List<CourseSectionDto> sections) {
        if (hasSchedule(semesterId))
            throw new ScheduleAlreadyExistsException(semesterId);

        var semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new SemesterNotFoundException(semesterId));

        for (var sectionDto : sections) {
            var courseSection = courseSectionRepository.save(createCourseSection(sectionDto, semester));

            for (var timeRange : sectionDto.timeRanges()) {
                var timeSlot = CourseSectionTimeSlot.builder()
                    .courseSection(courseSection)
                    .weekday(timeRange.weekday())
                    .startHour(timeRange.start())
                    .endHour(timeRange.end())
                    .build();

                courseSectionTimeSlotRepository.save(timeSlot);
            }

            for (var student : sectionDto.students()) {
                var enrollment = CourseSectionStudent.builder()
                    .student(Student.builder().id(student.id()).build())
                    .courseSection(courseSection)
                    .build();

                courseSectionStudentRepository.save(enrollment);
            }
        }
    }

    @Transactional
    public void deleteSchedule(Integer semesterId) {
        courseSectionRepository.deleteBySemesterId(semesterId);
    }

    private CourseSection createCourseSection(CourseSectionDto dto, Semester semester) {
        return CourseSection.builder()
            .semester(semester)
            .sectionNumber(dto.section())
            .course(Course.builder().id(dto.course().id()).build())
            .teacher(Teacher.builder().id(dto.teacher().id()).build())
            .classroom(Classroom.builder().id(dto.classroom().id()).build())
            .build();
    }

    private static SemesterType getSemesterType(int orderInYear) {
        return orderInYear == 1 ? FALL : SPRING;
    }

    private CourseSectionDto toDto(
        CourseSection section,
        Map<Integer, CourseData> courses,
        Map<Integer, TeacherData> teachers,
        Map<Integer, ClassroomData> classrooms,
        Map<Integer, StudentData> students
    ) {
        var timeSlots = courseSectionTimeSlotRepository.findByCourseSectionId(section.getId());
        var enrollments = courseSectionStudentRepository.findByCourseSectionId(section.getId());

        var timeRanges = timeSlots.stream()
            .map(slot -> new TimeRange(slot.getWeekday(), slot.getStartHour(), slot.getEndHour()))
            .toList();

        var sectionStudents = enrollments.stream()
            .map(enrollment -> students.get(enrollment.getStudent().getId()))
            .toList();

        return new CourseSectionDto(
            courses.get(section.getCourse().getId()),
            section.getSectionNumber(),
            timeRanges,
            teachers.get(section.getTeacher().getId()),
            classrooms.get(section.getClassroom().getId()),
            sectionStudents
        );
    }
}

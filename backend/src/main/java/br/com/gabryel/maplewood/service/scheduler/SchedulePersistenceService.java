package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.db.Classroom;
import br.com.gabryel.maplewood.model.db.Course;
import br.com.gabryel.maplewood.model.db.CourseSection;
import br.com.gabryel.maplewood.model.db.CourseSectionStudent;
import br.com.gabryel.maplewood.model.db.CourseSectionTimeSlot;
import br.com.gabryel.maplewood.model.db.Semester;
import br.com.gabryel.maplewood.model.db.Student;
import br.com.gabryel.maplewood.model.db.Teacher;
import br.com.gabryel.maplewood.model.dto.CourseSectionDto;
import br.com.gabryel.maplewood.repository.CourseSectionRepository;
import br.com.gabryel.maplewood.repository.CourseSectionStudentRepository;
import br.com.gabryel.maplewood.repository.CourseSectionTimeSlotRepository;
import br.com.gabryel.maplewood.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulePersistenceService {
    private final CourseSectionRepository courseSectionRepository;
    private final CourseSectionTimeSlotRepository courseSectionTimeSlotRepository;
    private final CourseSectionStudentRepository courseSectionStudentRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public void persistSchedule(Integer semesterId, List<CourseSectionDto> sections) {
        // TODO Block generation when already exists
        courseSectionRepository.deleteBySemesterId(semesterId);

        var semester = semesterRepository.findById(semesterId)
            .orElseThrow(() -> new IllegalArgumentException("Semester not found"));

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

    private CourseSection createCourseSection(CourseSectionDto dto, Semester semester) {
        return CourseSection.builder()
            .semester(semester)
            .sectionNumber(dto.section())
            .course(Course.builder().id(dto.course().id()).build())
            .teacher(Teacher.builder().id(dto.course().id()).build())
            .classroom(Classroom.builder().id(dto.course().id()).build())
            .build();
    }
}
package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.db.Teacher;
import br.com.gabryel.maplewood.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class TeacherDataService {
    private final TeacherRepository teacherRepository;

    public record TeacherData(int id, String name, int specializationId, int maxDailyHours) {
    }

    public Map<Integer, TeacherData> getTeachers() {
        return teacherRepository.findAll().stream().collect(toMap(
            Teacher::getId,
            teacher -> new TeacherData(
                teacher.getId(),
                teacher.getFirstName() + " " + teacher.getLastName(),
                teacher.getSpecialization().getId(),
                teacher.getMaxDailyHours()
            )
        ));
    }
}

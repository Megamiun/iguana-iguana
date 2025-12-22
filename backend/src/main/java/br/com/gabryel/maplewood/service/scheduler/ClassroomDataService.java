package br.com.gabryel.maplewood.service.scheduler;

import br.com.gabryel.maplewood.model.db.Classroom;
import br.com.gabryel.maplewood.model.db.Specialization;
import br.com.gabryel.maplewood.repository.ClassroomRepository;
import br.com.gabryel.maplewood.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class ClassroomDataService {
    private final ClassroomRepository classroomRepository;
    private final SpecializationRepository specializationRepository;

    public record RoomTypeData(List<Integer> specializationIds) {}
    public record ClassroomData(int id, String name, int capacity, RoomTypeData roomType) {}

    public Map<Integer, ClassroomData> getClassrooms() {
        var specializationsByRoomType = specializationRepository.findAll().stream()
            .filter(spec -> spec.getRoomType() != null)
            .collect(groupByRoomType());

        return classroomRepository.findAll().stream().collect(toMap(
            Classroom::getId,
            classroom -> new ClassroomData(
                classroom.getId(),
                classroom.getName(),
                classroom.getCapacity(),
                specializationsByRoomType.get(classroom.getRoomType().getId())
            )
        ));
    }

    private static Collector<Specialization, ?, Map<Integer, RoomTypeData>> groupByRoomType() {
        return groupingBy(
            specialization -> specialization.getRoomType().getId(),
            mapping(Specialization::getId, collectingAndThen(toList(), RoomTypeData::new))
        );
    }
}

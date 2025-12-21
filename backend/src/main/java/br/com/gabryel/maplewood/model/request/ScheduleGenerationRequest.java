package br.com.gabryel.maplewood.model.request;

import br.com.gabryel.maplewood.model.SemesterType;
import lombok.Data;

@Data
public class ScheduleGenerationRequest {
    private SemesterType semester;
    private Integer year;
}

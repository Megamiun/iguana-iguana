package br.com.gabryel.maplewood.model.request;

import br.com.gabryel.maplewood.model.Semester;
import lombok.Data;

@Data
public class ScheduleGenerationRequest {
    private Semester semester;
    private Integer year;
}

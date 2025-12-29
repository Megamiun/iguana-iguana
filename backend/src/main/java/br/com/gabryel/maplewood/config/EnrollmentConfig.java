package br.com.gabryel.maplewood.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "maplewood.enrollment")
public class EnrollmentConfig {
    private int maxPerSemester;
}

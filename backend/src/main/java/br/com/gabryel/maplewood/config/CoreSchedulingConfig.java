package br.com.gabryel.maplewood.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "maplewood.scheduling.algorithm.core")
public class CoreSchedulingConfig {
    private int maxSlack;
    private int maxCombinations;
}

package br.com.gabryel.maplewood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MaplewoodApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaplewoodApplication.class, args);
    }
}

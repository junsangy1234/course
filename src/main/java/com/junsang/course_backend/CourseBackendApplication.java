package com.junsang.course_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CourseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseBackendApplication.class, args);
	}

}

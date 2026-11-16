package com.student.taskprioritizer;

import com.student.taskprioritizer.config.SuggestionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SuggestionProperties.class)
public class TaskPrioritizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskPrioritizerApplication.class, args);
    }
}

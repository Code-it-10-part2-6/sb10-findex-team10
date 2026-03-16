package com.sb10findexteam6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class Sb10FindexTeam6Application {

    public static void main(String[] args) {
        SpringApplication.run(Sb10FindexTeam6Application.class, args);
    }

}

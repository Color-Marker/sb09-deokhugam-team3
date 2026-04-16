package com.sb09.deokhugam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class DeokhugamApplication {

  public static void main(String[] args) {
    SpringApplication.run(DeokhugamApplication.class, args);
  }

}

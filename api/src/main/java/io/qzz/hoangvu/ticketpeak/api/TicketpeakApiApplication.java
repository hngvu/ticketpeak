package io.qzz.hoangvu.ticketpeak.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableJpaAuditing
@EnableMethodSecurity
@EnableAsync
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class TicketpeakApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketpeakApiApplication.class, args);
    }

}

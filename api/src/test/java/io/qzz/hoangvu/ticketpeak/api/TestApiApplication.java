package io.qzz.hoangvu.ticketpeak.api;

import org.springframework.boot.SpringApplication;

public class TestApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(TicketpeakApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}

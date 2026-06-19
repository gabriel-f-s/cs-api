package com.dev.cs_api;

import org.springframework.boot.SpringApplication;

public class TestCsApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(Startup::main).with(TestcontainersConfiguration.class).run(args);
    }

}

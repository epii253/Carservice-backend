package ru.svyatniy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        System.out.println("you shouldn't be here");

        SpringApplication.run(Main.class, args);
    }
}
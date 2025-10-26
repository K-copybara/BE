package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
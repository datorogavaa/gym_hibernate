package com.crm.hibernate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HibernateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HibernateApplication.class, args);
    }
}

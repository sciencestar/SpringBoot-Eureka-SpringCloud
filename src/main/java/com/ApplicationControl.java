package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableEurekaServer
public class ApplicationControl {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationControl.class,args);
    }

}

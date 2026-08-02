package com.guanxuege.grade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan("com.guanxuege.grade.mapper")
@SpringBootApplication(scanBasePackages = "com.guanxuege")
public class GradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeApplication.class, args);
    }
}

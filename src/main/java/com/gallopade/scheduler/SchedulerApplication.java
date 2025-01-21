package com.gallopade.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileNotFoundException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class SchedulerApplication {

    @Value("${scheduler.previousData:false}")
    public Boolean previousData;

    public static void main(String[] args) throws FileNotFoundException, NoSuchAlgorithmException, KeyManagementException {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}

















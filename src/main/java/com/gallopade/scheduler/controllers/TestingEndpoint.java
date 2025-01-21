package com.gallopade.scheduler.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/start-testing")
public class TestingEndpoint {

    private final ApiTesting apiTesting;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String syncSpecificScheduler() throws NoSuchAlgorithmException, KeyManagementException, FileNotFoundException {
        var tokens = apiTesting.createUserTokens();
        System.out.println(tokens);
        apiTesting.sendRequests(tokens);
        return "Successfully sent testing requests";
    }
}















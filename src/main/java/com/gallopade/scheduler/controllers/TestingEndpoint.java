package com.gallopade.scheduler.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Testing", description = "API endpoints for testing student account operations")
public class TestingEndpoint {

    private final ApiTesting apiTesting;

    @Operation(
            summary = "Start testing process",
            description = "Initiates the testing process by loading student accounts from CSV file, " +
                    "creating authentication tokens for each student, and sending test requests to the gradebook service."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Testing process completed successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - could be due to CSV file not found, " +
                            "authentication failures, or network issues"
            )
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public String syncSpecificScheduler() throws NoSuchAlgorithmException, KeyManagementException, FileNotFoundException {
        var tokenWithAssignments = apiTesting.createUserTokens();
        System.out.println("Created " + tokenWithAssignments.size() + " tokens with assignments:");
        tokenWithAssignments.forEach(ta -> System.out.println("Token: " + ta.getToken() + ", AssignmentId: " + ta.getStudentClassAssignmentId()));
        apiTesting.sendRequests(tokenWithAssignments);
        return "Successfully sent testing requests";
    }
}















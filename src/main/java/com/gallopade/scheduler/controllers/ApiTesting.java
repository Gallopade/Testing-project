package com.gallopade.scheduler.controllers;

import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
class ApiTesting {
    private Gson gson = new Gson();
    private String baseUrl = "https://demo.gallopade.com/";
    private String csvFile = "student-account.csv";
    private String questionIdsFile = "question-ids.json";
    private Map<Integer, QuestionIdMapping> questionMap = null;

    private void disableCertificateValidation() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    }

    private String sendRequest(String url, HttpEntity<?> entity, HttpMethod method) {
        try {
            URI uri = new URL(url).toURI();
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.exchange(uri, method, entity, String.class).getBody();
            System.out.println("[REQUEST] " + method + " " + url + " - Response length: " + (response != null ? response.length() : 0));
            return response;
        } catch (Exception e) {
            System.out.println("[REQUEST ERROR] " + method + " " + url);
            System.out.println("[REQUEST ERROR] Exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            System.out.println("[REQUEST ERROR] Entity: " + entity);
        }

        return "";
    }

    private HttpHeaders setHeaders(String authorization, String contentType, String accept) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        headers.set("Content-Type", contentType);
        headers.set("Accept", accept);
        return headers;
    }

    private HttpEntity<?> createUserRequest(CSVRecord record) {
        String authString = "c3ByaW5nYmFua0NsaWVudDpzcHJpbmdiYW5rU2VjcmV0";

        HttpHeaders headers = setHeaders("Basic " + authString, "application/x-www-form-urlencoded", "application/json");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", record.get("username"));
        formData.add("password", record.get("password"));

        return new HttpEntity<>(formData, headers);
    }

    private HttpEntity<?> saveStudentClassAssignment(String token, String studentClassAssignmentId, String questionNumber) {
        HttpHeaders headers = setHeaders("Bearer " + token, "application/json", "application/json");
        return new HttpEntity<>(Payloads.getSaveStudentAnswer(studentClassAssignmentId, questionNumber), headers);
    }
    
    // Overloaded method for backward compatibility
    private HttpEntity<?> saveStudentClassAssignment(String token, String studentClassAssignmentId) {
        return saveStudentClassAssignment(token, studentClassAssignmentId, null);
    }

    /**
     * Fetches a question by question ID and studentClassAssignmentId
     * @param token Bearer token for authentication
     * @param questionId The question ID to fetch
     * @param studentClassAssignmentId The student class assignment ID
     * @return The question response as a String
     */
    public String getQuestion(String token, String questionId, String studentClassAssignmentId) {
        String url = baseUrl + "question-query-service/api/v1/questionLookup/byId/" + questionId + 
                     "?studentClassAssignmentId=" + studentClassAssignmentId;
        
        HttpHeaders headers = setHeaders("Bearer " + token, "application/json", "application/json");
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        return sendRequest(url, entity, HttpMethod.GET);
    }
    
    /**
     * Loads question payloads from JSON file in resources
     */
    private void loadQuestionIds() {
        if (questionMap != null) {
            return; // Already loaded
        }
        
        try (Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(questionIdsFile)
        )) {
            if (reader == null) {
                System.out.println("Warning: question-ids.json not found");
                questionMap = new HashMap<>();
                return;
            }
            
            QuestionIdMapping[] mappings = gson.fromJson(reader, QuestionIdMapping[].class);
            questionMap = Arrays.stream(mappings)
                    .collect(Collectors.toMap(
                            QuestionIdMapping::getQuestionNumber,
                            mapping -> mapping
                    ));
            System.out.println("Loaded " + questionMap.size() + " question payloads from JSON file");
        } catch (IOException e) {
            System.out.println("Error loading question-ids.json: " + e.getMessage());
            questionMap = new HashMap<>();
        }
    }
    
    /**
     * Gets question ID for a given question number (1-21)
     * @param questionNumber Question number (1-21)
     * @return Question ID or null if invalid
     */
    public String getQuestionIdByNumber(int questionNumber) {
        loadQuestionIds();
        QuestionIdMapping mapping = questionMap.get(questionNumber);
        if (mapping != null && mapping.getQuestion() != null) {
            Map<String, Object> question = mapping.getQuestion();
            Map<String, Object> studentClassAssignmentAnswer = (Map<String, Object>) question.get("studentClassAssignmentAnswer");
            if (studentClassAssignmentAnswer != null) {
                return (String) studentClassAssignmentAnswer.get("questionId");
            }
        }
        return null;
    }
    
    /**
     * Gets full question payload for a given question number (1-21)
     * @param questionNumber Question number (1-21)
     * @return Question payload map or null if invalid
     */
    public Map<String, Object> getQuestionPayloadByNumber(int questionNumber) {
        loadQuestionIds();
        QuestionIdMapping mapping = questionMap.get(questionNumber);
        return mapping != null ? mapping.getQuestion() : null;
    }

    public void sendRequests(List<TokenWithAssignment> tokenWithAssignments) {
        int requestsPerStudent = 1000;
        int totalQuestions = 21;
        int minRequestsPerQuestion = 2; // Each question sent at least 10 times
        int totalStudents = tokenWithAssignments.size();
        int totalRequests = requestsPerStudent * totalStudents;
        
        System.out.println("========================================");
        System.out.println("Starting request sending process:");
        System.out.println("Total students: " + totalStudents);
        System.out.println("Requests per student: " + requestsPerStudent);
        System.out.println("Total questions: " + totalQuestions);
        System.out.println("Minimum requests per question: " + minRequestsPerQuestion);
        System.out.println("Total requests to send: " + totalRequests);
        System.out.println("========================================");
        
        final int[] completedStudents = {0};
        
        tokenWithAssignments.stream().parallel()
                .forEach(ta -> {
                    String studentId = ta.getStudentClassAssignmentId();
                    System.out.println("[Student " + (completedStudents[0] + 1) + "/" + totalStudents + "] Starting " + requestsPerStudent + " requests for student with assignmentId: " + studentId);
                    
                    // First, send each question at least minRequestsPerQuestion times
                    IntStream.range(1, totalQuestions + 1).parallel()
                            .forEach(questionNumber -> {
                                String questionNumberStr = String.valueOf(questionNumber);
                                String url = baseUrl + "gradebook-command-service/api/v1/saveStudentClassAssignmentAnswer";
                                
                                for (int j = 0; j < minRequestsPerQuestion; j++) {
                                    System.out.println("[REQUEST] Student: " + studentId + " | Question: " + questionNumber + " | Request #" + (j + 1) + "/" + minRequestsPerQuestion + " (min) | URL: " + url);
                                    
                                    HttpEntity<?> entity = this.saveStudentClassAssignment(ta.getToken(), ta.getStudentClassAssignmentId(), questionNumberStr);
                                    String response = this.sendRequest(url, entity, HttpMethod.POST);
                                    
                                    if (response != null && !response.isEmpty()) {
                                        try {
                                            HashMap<?, ?> jsonResponse = gson.fromJson(response, HashMap.class);
                                            Object message = jsonResponse.get("message");
                                            System.out.println("[RESPONSE] Student: " + studentId + " | Question: " + questionNumber + " | Request #" + (j + 1) + " | Response: " + (message != null ? message : "Success"));
                                        } catch (Exception e) {
                                            System.out.println("[RESPONSE] Student: " + studentId + " | Question: " + questionNumber + " | Request #" + (j + 1) + " | Response received");
                                        }
                                    }
                                }
                            });
                    
                    // Then, send remaining requests cycling through questions
                    int remainingRequests = requestsPerStudent - (totalQuestions * minRequestsPerQuestion);
                    if (remainingRequests > 0) {
                        System.out.println("[Student " + studentId + "] Sending remaining " + remainingRequests + " requests (cycling through questions)");
                        
                        IntStream.range(0, remainingRequests).parallel()
                                .mapToObj(i -> {
                                    int questionNumber = (i % totalQuestions) + 1;
                                    String questionNumberStr = String.valueOf(questionNumber);
                                    String url = baseUrl + "gradebook-command-service/api/v1/saveStudentClassAssignmentAnswer";
                                    
                                    int requestNum = (totalQuestions * minRequestsPerQuestion) + i + 1;
                                    System.out.println("[REQUEST] Student: " + studentId + " | Question: " + questionNumber + " | Request #" + requestNum + "/" + requestsPerStudent + " | URL: " + url);
                                    
                                    HttpEntity<?> entity = this.saveStudentClassAssignment(ta.getToken(), ta.getStudentClassAssignmentId(), questionNumberStr);
                                    String response = this.sendRequest(url, entity, HttpMethod.POST);
                                    
                                    if (i % 100 == 0 && i > 0) {
                                        System.out.println("[PROGRESS] [Student " + studentId + "] Progress: " + requestNum + "/" + requestsPerStudent + " requests sent (current question: " + questionNumber + ")");
                                    }
                                    
                                    return response;
                                })
                                .forEach(s -> {
                                    // Response already logged above
                                });
                    }
                    
                    synchronized (completedStudents) {
                        completedStudents[0]++;
                        System.out.println("[Student " + completedStudents[0] + "/" + totalStudents + "] Completed " + requestsPerStudent + " requests for student: " + studentId);
                    }
                });
        
        System.out.println("========================================");
        System.out.println("All requests completed!");
        System.out.println("Total students processed: " + totalStudents);
        System.out.println("Total requests sent: " + totalRequests + " (" + requestsPerStudent + " requests × " + totalStudents + " students)");
        System.out.println("Each question sent at least " + minRequestsPerQuestion + " times per student");
        System.out.println("========================================");
    }

    public List<TokenWithAssignment> createUserTokens() throws NoSuchAlgorithmException, KeyManagementException {
        disableCertificateValidation();
        System.out.println("File Load Process...");
        try (Reader reader = new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(csvFile)
        )) {

            if (reader == null) {
                throw new FileNotFoundException("student-account.csv not found in resources");
            }

            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

            return csvParser.getRecords().stream().parallel()
                    .map(record -> {
                        String studentClassAssignmentId = record.get("studentClassAssignmentId");
                        HttpEntity<?> request = createUserRequest(record);
                        String response = sendRequest(baseUrl + "oauth20-service/oauth/token", request, HttpMethod.POST);
                        if (response != null && !response.isEmpty()) {
                            try {
                                String token = gson.fromJson(response, HashMap.class).get("access_token").toString();
                                return new TokenWithAssignment(token, studentClassAssignmentId);
                            } catch (Exception e) {
                                System.out.println("Failed to extract token from response: " + e.getMessage());
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(ta -> ta != null)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

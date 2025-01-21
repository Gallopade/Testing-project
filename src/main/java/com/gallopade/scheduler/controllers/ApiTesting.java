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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;


@Service
class ApiTesting {
    private Gson gson = new Gson();
    private String baseUrl = "https://go.gallopade.com/";
    private String csvFile = "/Users/gallopade/Documents/Gallopade/LMS-Scheduler/report.scheduler/src/main/resources/student-account.csv";

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
            return restTemplate.exchange(uri, method, entity, String.class).getBody();
        } catch (Exception e) {
            System.out.println(entity);
            System.out.println("exception is " + e);
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

    private HttpEntity<?> saveStudentClassAssignment(String token) {
        HttpHeaders headers = setHeaders("Bearer " + token, "application/json", "application/json");
        return new HttpEntity<>(Payloads.saveStudentAnswer, headers);
    }

    private HttpEntity<?> getQuestion(String token) {
        HttpHeaders headers = setHeaders("Bearer " + token, "application/json", "application/json");
        return new HttpEntity<>(Payloads.saveStudentAnswer, headers);
    }

    public void sendRequests(List<String> tokens) {
        tokens.stream().parallel()
                .map(this::saveStudentClassAssignment)
                .flatMap(e -> IntStream.range(0, 1).parallel().mapToObj(a -> this.sendRequest(baseUrl + "gradebook-command-service/api/v1/saveStudentClassAssignmentAnswer", e, HttpMethod.POST)))
                .forEach(s -> System.out.println(gson.fromJson(s, HashMap.class).get("message")));
    }

    public List<String> createUserTokens() throws NoSuchAlgorithmException, KeyManagementException, FileNotFoundException {
        disableCertificateValidation();
        try (Reader reader = new FileReader(csvFile); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            return csvParser.getRecords().stream().parallel()
                    .map(this::createUserRequest)
                    .map(e -> this.sendRequest(baseUrl + "oauth20-service/oauth/token", e, HttpMethod.POST))
                    .filter(e -> !e.isEmpty())
                    .map(s -> gson.fromJson(s, HashMap.class).get("access_token").toString())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

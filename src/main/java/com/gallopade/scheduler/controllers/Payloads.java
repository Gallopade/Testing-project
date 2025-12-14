package com.gallopade.scheduler.controllers;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Payloads {
    private static final Gson gson = new Gson();
    private static final String questionIdsFile = "question-ids.json";
    private static Map<Integer, QuestionIdMapping> questionMap = null;
    static String saveStudentAnswer1 = "{\n" +
            "    \"studentClassAssignmentAnswer\": {\n" +
            "        \"studentClassAssignmentId \": \"23e65b46-ae2a-4285-91ec-647c072c0fc5\",\n" +
            "        \"classId\": \"2bae9970-18bd-464d-a6b1-24ef81012a8f\",\n" +
            "        \"contentDetailId\": \"76B719B02FE14590AAC0860F857B9A7B\",\n" +
            "        \"questionId\": \"AP_315E3075E72C493A9D29AFDDC0F0A64B\",\n" +
            "        \"questionFlag\": false,\n" +
            "        \"interactionType\": \"choice-interaction-single\",\n" +
            "        \"multiPart\": false,\n" +
            "        \"partNumber\": 1\n" +
            "    },\n" +
            "    \"studentClassAssignmentAnswerDetails\": [\n" +
            "        {\n" +
            "            \"id\": {\n" +
            "                \"studentClassAssignmentAnswerId\": null,\n" +
            "                \"choiceIdentifier\": \"a4e13e091-b3e6-3ccf-de84-ea1972956656\",\n" +
            "                \"gapIdentifier\": -1\n" +
            "            },\n" +
            "            \"studentAnswerText\": -1\n" +
            "        }\n" +
            "    ],\n" +
            "    \"studentClassAssignmentAnswerDuration\": {\n" +
            "        \"startTime\": 1737437777376,\n" +
            "        \"endTime\": 1737437962563\n" +
            "    }\n" +
            "}";


    /**
     * Loads question payloads from JSON file in resources
     */
    private static void loadQuestionIds() {
        if (questionMap != null) {
            return; // Already loaded
        }
        
        try (Reader reader = new InputStreamReader(
                Payloads.class.getClassLoader().getResourceAsStream(questionIdsFile)
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
            System.out.println("Payloads: Loaded " + questionMap.size() + " question payloads from JSON file");
        } catch (Exception e) {
            System.out.println("Error loading question-ids.json in Payloads: " + e.getMessage());
            questionMap = new HashMap<>();
        }
    }
    
    public static String getSaveStudentAnswer(String studentClassAssignmentId, String questionNumber) {
        return getSaveStudentAnswer(studentClassAssignmentId, questionNumber, null, null);
    }
    
    public static String getSaveStudentAnswer(String studentClassAssignmentId, String questionNumber, String classId, String classAssignmentId) {
        if (questionNumber != null) {
            try {
                int questionNum = Integer.parseInt(questionNumber);
                if (questionNum >= 1 && questionNum <= 21) {
                    loadQuestionIds();
                    QuestionIdMapping mapping = questionMap.get(questionNum);
                    
                    if (mapping != null && mapping.getQuestion() != null) {
                        // Deep copy the question payload to avoid modifying the original
                        String questionJson = gson.toJson(mapping.getQuestion());
                        Map<String, Object> questionPayload = gson.fromJson(questionJson, Map.class);
                        
                        // Update studentClassAssignmentId in the payload with the value from API/CSV
                        Map<String, Object> studentClassAssignmentAnswer = (Map<String, Object>) questionPayload.get("studentClassAssignmentAnswer");
                        if (studentClassAssignmentAnswer != null) {
                            // Replace the hardcoded studentClassAssignmentId from JSON with the actual one
                            studentClassAssignmentAnswer.put("studentClassAssignmentId", studentClassAssignmentId);
                            
                            // Update classId if provided from API response
                            if (classId != null && !classId.isEmpty()) {
                                studentClassAssignmentAnswer.put("classId", classId);
                            }
                        }
                        
                        // Update timestamps dynamically
                        Map<String, Object> duration = (Map<String, Object>) questionPayload.get("studentClassAssignmentAnswerDuration");
                        if (duration != null) {
                            long currentTime = System.currentTimeMillis();
                            long startTime = currentTime - 20000; // 20 seconds ago
                            duration.put("startTime", startTime);
                            duration.put("endTime", currentTime);
                        }
                        
                        // Convert back to JSON string with updated values
                        return gson.toJson(questionPayload);
                    }
                }
            } catch (NumberFormatException e) {
                // If questionNumber is not a valid integer, fall through to default
            } catch (Exception e) {
                System.out.println("Error processing question payload: " + e.getMessage());
            }
        }
        // Default payload when questionNumber is null or out of range
        String defaultClassId = classId != null && !classId.isEmpty() ? classId : "b8371cce-3305-4736-9ccb-744f13efc8fe";
        return "{\"studentClassAssignmentAnswer\":{\"studentClassAssignmentId\":\""+studentClassAssignmentId+"\",\"classId\":\""+defaultClassId+"\",\"contentDetailId\":\"03E5DA7B0DCE404BACB14CE2C97B57C9\",\"questionId\":\"88b0972c-ca59-4e8d-9fcc-b97fec0b3788\",\"questionFlag\":false,\"interactionType\":\"graphic-gap-match-interaction\",\"multiPart\":false,\"partNumber\":1},\"studentClassAssignmentAnswerDetails\":[{\"id\":{\"studentClassAssignmentAnswerId\":null,\"choiceIdentifier\":\"choice-0377889bbcef-9ccf-48de-59ac-027889bc3\",\"gapIdentifier\":\"cell0\"},\"studentAnswerText\":-1},{\"id\":{\"studentClassAssignmentAnswerId\":null,\"choiceIdentifier\":\"choice-0377889bbcef-9ccf-48de-59ac-027889bc4\",\"gapIdentifier\":\"cell1\"},\"studentAnswerText\":-1},{\"id\":{\"studentClassAssignmentAnswerId\":null,\"choiceIdentifier\":\"choice-0377889bbcef-9ccf-48de-59ac-027889bc2\",\"gapIdentifier\":\"cell2\"},\"studentAnswerText\":-1},{\"id\":{\"studentClassAssignmentAnswerId\":null,\"choiceIdentifier\":\"choice-0377889bbcef-9ccf-48de-59ac-027889bc1\",\"gapIdentifier\":\"cell3\"},\"studentAnswerText\":-1},{\"id\":{\"studentClassAssignmentAnswerId\":null,\"choiceIdentifier\":\"choice-0377889bbcef-9ccf-48de-59ac-027889bc0\",\"gapIdentifier\":\"cell4\"},\"studentAnswerText\":-1}],\"studentClassAssignmentAnswerDuration\":{\"startTime\":1765705737250,\"endTime\":1765705762120}}";
    }
}

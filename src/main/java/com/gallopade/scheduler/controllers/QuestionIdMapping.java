package com.gallopade.scheduler.controllers;

import java.util.Map;

public class QuestionIdMapping {
    private int questionNumber;
    private Map<String, Object> question;

    public QuestionIdMapping() {
    }

    public QuestionIdMapping(int questionNumber, Map<String, Object> question) {
        this.questionNumber = questionNumber;
        this.question = question;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public Map<String, Object> getQuestion() {
        return question;
    }

    public void setQuestion(Map<String, Object> question) {
        this.question = question;
    }
}


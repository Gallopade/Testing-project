package com.gallopade.scheduler.controllers;

public class TokenWithAssignment {
    private String token;
    private String studentClassAssignmentId;

    public TokenWithAssignment(String token, String studentClassAssignmentId) {
        this.token = token;
        this.studentClassAssignmentId = studentClassAssignmentId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStudentClassAssignmentId() {
        return studentClassAssignmentId;
    }

    public void setStudentClassAssignmentId(String studentClassAssignmentId) {
        this.studentClassAssignmentId = studentClassAssignmentId;
    }

    @Override
    public String toString() {
        return "TokenWithAssignment{" +
                "token='" + token + '\'' +
                ", studentClassAssignmentId='" + studentClassAssignmentId + '\'' +
                '}';
    }
}


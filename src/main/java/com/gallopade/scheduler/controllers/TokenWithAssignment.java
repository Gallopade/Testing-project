package com.gallopade.scheduler.controllers;

public class TokenWithAssignment {
    private String token;
    private String studentClassAssignmentId;
    private String classAssignmentId;
    private String classId;

    public TokenWithAssignment(String token, String studentClassAssignmentId) {
        this.token = token;
        this.studentClassAssignmentId = studentClassAssignmentId;
    }

    public TokenWithAssignment(String token, String studentClassAssignmentId, String classAssignmentId, String classId) {
        this.token = token;
        this.studentClassAssignmentId = studentClassAssignmentId;
        this.classAssignmentId = classAssignmentId;
        this.classId = classId;
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

    public String getClassAssignmentId() {
        return classAssignmentId;
    }

    public void setClassAssignmentId(String classAssignmentId) {
        this.classAssignmentId = classAssignmentId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Override
    public String toString() {
        return "TokenWithAssignment{" +
                "token='" + token + '\'' +
                ", studentClassAssignmentId='" + studentClassAssignmentId + '\'' +
                ", classAssignmentId='" + classAssignmentId + '\'' +
                ", classId='" + classId + '\'' +
                '}';
    }
}


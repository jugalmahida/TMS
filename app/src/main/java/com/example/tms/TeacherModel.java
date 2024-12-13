package com.example.tms;

import java.util.ArrayList;

public class TeacherModel {
    protected String tcName;
    private String fullName;
    private ArrayList<String> standards;
    private ArrayList<String> subjects;
    private String email;
    private String phone;
    public TeacherModel(){};

    public TeacherModel(String tcName, String fullName, ArrayList<String> standards, ArrayList<String> subjects, String email, String phone) {
        this.tcName = tcName;
        this.fullName = fullName;
        this.standards = standards;
        this.subjects = subjects;
        this.email = email;
        this.phone = phone;
    }

    public String getTcName() {
        return tcName;
    }

    public void setTcName(String tcName) {
        this.tcName = tcName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public ArrayList<String> getStandards() {
        return standards;
    }

    public void setStandards(ArrayList<String> standards) {
        this.standards = standards;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<String> subjects) {
        this.subjects = subjects;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "TeacherModel{" +
                "tcName='" + tcName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", standards=" + standards +
                ", subjects=" + subjects +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}

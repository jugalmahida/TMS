package com.example.tms;

public class AnnouncementModel {
    String fullName,time,announcement,tcName,id,imgUrl;
    public AnnouncementModel(){}
    public AnnouncementModel(String fullName, String time, String announcement, String tcName) {
        this.fullName = fullName;
        this.time = time;
        this.announcement = announcement;
        this.tcName = tcName;
    }
//    public AnnouncementModel(String fullName, String time, String announcement, String tcName, String id) {
//        this.fullName = fullName;
//        this.time = time;
//        this.announcement = announcement;
//        this.tcName = tcName;
//        this.id = id;
//    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public String getTcName() {
        return tcName;
    }

    public void setTcName(String tcName) {
        this.tcName = tcName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

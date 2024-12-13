package com.example.tms;

public class MaterialModel {
    private String fileUri;
    private String fileName;
    private String time;
    private String tcName;
    private String fileId;

    public MaterialModel(){}

    public MaterialModel(String fileUri, String fileName, String time, String tcName, String fileId) {
        this.fileUri = fileUri;
        this.fileName = fileName;
        this.time = time;
        this.tcName = tcName;
        this.fileId = fileId;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTcName() {
        return tcName;
    }

    public void setTcName(String tcName) {
        this.tcName = tcName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public String toString() {
        return "MaterialModel{" +
                "fileUri='" + fileUri + '\'' +
                ", fileName='" + fileName + '\'' +
                ", time='" + time + '\'' +
                ", tcName='" + tcName + '\'' +
                ", fileId='" + fileId + '\'' +
                '}';
    }
}

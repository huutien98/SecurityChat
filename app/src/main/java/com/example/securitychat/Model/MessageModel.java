package com.example.securitychat.Model;

public class MessageModel {

    String uId, message, mesageId;
    Long timestamp;

    public MessageModel(String uId, String message, Long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
    }



    public MessageModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }
    public MessageModel(){

    }

    public String getMesageId() {
        return mesageId;
    }

    public void setMesageId(String mesageId) {
        this.mesageId = mesageId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

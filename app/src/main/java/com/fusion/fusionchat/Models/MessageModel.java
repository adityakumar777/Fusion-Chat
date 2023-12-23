package com.fusion.fusionchat.Models;

public class MessageModel {
    String uid ,photo ,messageId ,message ,lastMessage;
    Long time;
    int feeling = -1;

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageModel(){} //Default Constructor is required for Firebase
    public MessageModel(String uid, String message, Long time) {
        this.uid = uid;
        this.message = message;
        this.time = time;
    }
    public String getUid() {
        return uid;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Long getTime() {
        return this.time;
    }
    public int getFeeling() {
        return feeling;
    }
    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }
    public String getLastMessage() {
        return lastMessage;
    }

}

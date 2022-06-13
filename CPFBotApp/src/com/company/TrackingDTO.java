package com.company;

public class TrackingDTO {

    private int trackingID;
    private String userName;
    private int chatFormID;
    private int chatMessageTemplateID;
    private boolean complete;
    private boolean replyRequired;

    public int getTrackingID() {
        return trackingID;
    }

    public boolean isReplyRequired() {
        return replyRequired;
    }

    public void setReplyRequired(boolean replyRequired) {
        this.replyRequired = replyRequired;
    }

    public void setTrackingID(int trackingID) {
        this.trackingID = trackingID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getChatFormID() {
        return chatFormID;
    }

    public void setChatFormID(int chatFormID) {
        this.chatFormID = chatFormID;
    }

    public int getChatMessageTemplateID() {
        return chatMessageTemplateID;
    }

    public void setChatMessageTemplateID(int chatMessageTemplateID) {
        this.chatMessageTemplateID = chatMessageTemplateID;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

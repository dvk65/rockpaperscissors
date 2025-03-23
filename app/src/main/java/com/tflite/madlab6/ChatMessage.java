package com.tflite.madlab6;

public class ChatMessage {
    private String sender;
    private String message;
    private String choice;
    private long timestamp;
    private int userScore;

    public ChatMessage() {}

    public ChatMessage(String sender, String message, String choice, int userScore) {
        this.sender = sender;
        this.message = message;
        this.choice = choice;
        this.userScore = userScore;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }
    public String getMessage() {
        return message;
    }
    public String getChoice() {
        return choice;
    }
    public int getUserScore() { return userScore; }
    public long getTimestamp() {
        return timestamp;
    }
}

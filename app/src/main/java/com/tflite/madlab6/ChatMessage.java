package com.tflite.madlab6;

/** @noinspection unused*/
public class ChatMessage {
    private String sender;
    private String message;
    private String choice;
    private long timestamp;
    private int userScore;
    private String turn;

    public ChatMessage() {
        // Default constructor required for Firebase
    }

    public ChatMessage(String sender, String message, String choice, int userScore, String turn) {
        this.sender = sender;
        this.message = message;
        this.choice = choice;
        this.userScore = userScore;
        this.timestamp = System.currentTimeMillis();
        this.turn = turn;
    }

    public String getSender() {
        return sender;
    }
    public String getMessage() { return message; }
    public String getChoice() {
        return choice;
    }
    public int getUserScore() { return userScore; }
    public String getTurn() { return turn; }
    public long getTimestamp() { return timestamp; }
}

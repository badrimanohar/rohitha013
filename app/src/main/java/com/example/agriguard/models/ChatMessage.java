package com.example.agriguard.models;

public class ChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String text;
    private String image;
    private long timestamp;

    public ChatMessage() {
        // Required for Firebase
    }

    public ChatMessage(String id, String senderId, String senderName, String text, String image, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.image = image;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public String getImage() { return image; }
    public long getTimestamp() { return timestamp; }
}

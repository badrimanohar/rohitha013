package com.example.agriguard.models;

public class CommunityPost {
    private String author;
    private String time;
    private String content;
    private int imageResId;
    private int likes;
    private int comments;

    public CommunityPost(String author, String time, String content, int imageResId, int likes, int comments) {
        this.author = author;
        this.time = time;
        this.content = content;
        this.imageResId = imageResId;
        this.likes = likes;
        this.comments = comments;
    }

    // Getters
    public String getAuthor() { return author; }
    public String getTime() { return time; }
    public String getContent() { return content; }
    public int getImageResId() { return imageResId; }
    public int getLikes() { return likes; }
    public int getComments() { return comments; }
}

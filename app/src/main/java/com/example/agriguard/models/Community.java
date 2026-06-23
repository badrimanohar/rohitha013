package com.example.agriguard.models;

public class Community {
    private String id;
    private String name;
    private String description;
    private int memberCount;
    private String lastMessage;
    private int unreadCount;
    private String image;
    private boolean isJoined;

    public Community() {
        // Required for Firebase
    }

    public Community(String id, String name, String description, int memberCount, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.image = image;
    }

    // Constructor used for the "Joined" list if needed locally
    public Community(String id, String name, String description, int memberCount, String lastMessage, int unreadCount, String image, boolean isJoined) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = memberCount;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.image = image;
        this.isJoined = isJoined;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isJoined() { return isJoined; }
    public void setJoined(boolean joined) { isJoined = joined; }
}

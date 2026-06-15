package com.example.agrinova;

import java.io.Serializable;
import java.util.Map;

public class Community implements Serializable {
    private String id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private int iconResId;
    private long memberCount;
    private Map<String, Boolean> members;
    private boolean isJoined;
    private String lastMessage;
    private long lastMessageTime;
    private int unreadCount;

    public Community() {
        // Required for Firebase
    }

    public Community(String id, String name, String description, String category, int iconResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.iconResId = iconResId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public long getMemberCount() { return memberCount; }
    public void setMemberCount(long memberCount) { this.memberCount = memberCount; }

    public Map<String, Boolean> getMembers() { return members; }
    public void setMembers(Map<String, Boolean> members) { this.members = members; }

    public boolean isJoined() { return isJoined; }
    public void setJoined(boolean joined) { isJoined = joined; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}

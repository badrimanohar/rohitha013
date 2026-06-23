package com.example.agriguard.models;

public class User {
    private String name;
    private String email;
    private String profileImage;
    private String provider;
    private long createdAt;
    private long updatedAt;
    private String phone;
    private String location;
    private String bio;
    private String preferredCrops;

    public User() {
        // Required for Firebase
    }

    public User(String name, String email, String profileImage, String provider, long createdAt) {
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.provider = provider;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProfileImage() { return profileImage; }
    public String getProvider() { return provider; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getPreferredCrops() { return preferredCrops; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPreferredCrops(String preferredCrops) { this.preferredCrops = preferredCrops; }
}

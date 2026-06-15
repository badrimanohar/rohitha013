package com.example.agrinova;

public class User {
    private String uid;
    private String name;
    private String email;
    private String mobile;
    private String profileImageUri;

    public User() {
        // Required for Firebase
    }

    public User(String uid, String name, String email, String profileImageUri) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImageUri = profileImageUri;
    }

    public User(String uid, String name, String email, String mobile, String profileImageUri) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.profileImageUri = profileImageUri;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getProfileImageUri() { return profileImageUri; }
    public void setProfileImageUri(String profileImageUri) { this.profileImageUri = profileImageUri; }

    // Alias for community feature if needed
    public String getProfileImage() { return profileImageUri; }
    public void setProfileImage(String profileImage) { this.profileImageUri = profileImage; }
}

package com.example.agrinova;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class PrefsManager {
    private static final String PREF_NAME = "AgriNovaPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_LANGUAGE = "selectedLanguage";
    private static final String KEY_USER_LIST = "userList";
    private static final String KEY_CURRENT_USER_EMAIL = "currentUserEmail";
    private static final String KEY_JOINED_COMMUNITIES = "joinedCommunities";
    private static final String KEY_HISTORY = "detectionHistory";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public PrefsManager(Context context) {
        if (context != null) {
            pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            editor = pref.edit();
            gson = new Gson();
        }
    }

    public void saveHistoryItem(String imageUri, String diseaseName, String confidence) {
        List<HistoryItem> history = getHistory();
        history.add(0, new HistoryItem(imageUri, diseaseName, confidence, System.currentTimeMillis()));
        editor.putString(KEY_HISTORY, gson.toJson(history)).apply();
    }

    public List<HistoryItem> getHistory() {
        String json = pref.getString(KEY_HISTORY, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<HistoryItem>>(){}.getType());
    }

    public void clearHistory() {
        editor.remove(KEY_HISTORY).apply();
    }

    public void deleteHistoryItem(int position) {
        List<HistoryItem> history = getHistory();
        if (position >= 0 && position < history.size()) {
            history.remove(position);
            editor.putString(KEY_HISTORY, gson.toJson(history)).apply();
        }
    }

    public static class HistoryItem {
        public String imageUri, diseaseName, confidence;
        public long timestamp;
        public HistoryItem(String imageUri, String diseaseName, String confidence, long timestamp) {
            this.imageUri = imageUri;
            this.diseaseName = diseaseName;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language).apply();
    }

    public String getLanguage() {
        return pref.getString(KEY_LANGUAGE, null);
    }

    public void saveUser(String name, String email, String password, String mobile, String imageUri) {
        List<LocalUser> users = getUserList();
        // Remove existing user with same email if exists
        users.removeIf(u -> u.email.equalsIgnoreCase(email));
        
        LocalUser newUser = new LocalUser(name, email, password, mobile, imageUri);
        users.add(newUser);
        
        String json = gson.toJson(users);
        editor.putString(KEY_USER_LIST, json).apply();
        setCurrentUser(email);
    }

    public List<LocalUser> getUserList() {
        String json = pref.getString(KEY_USER_LIST, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<LocalUser>>(){}.getType());
    }

    public void setCurrentUser(String email) {
        editor.putString(KEY_CURRENT_USER_EMAIL, email).apply();
    }

    public LocalUser getCurrentUser() {
        String email = pref.getString(KEY_CURRENT_USER_EMAIL, null);
        if (email == null) return null;
        for (LocalUser u : getUserList()) {
            if (u.email.equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    public String getUserName() {
        LocalUser user = getCurrentUser();
        return user != null ? user.name : "Farmer";
    }

    public String getProfileImage() {
        LocalUser user = getCurrentUser();
        return user != null ? user.profileImageUri : null;
    }

    public void setProfileImage(String uri) {
        LocalUser current = getCurrentUser();
        if (current != null) {
            saveUser(current.name, current.email, current.password, current.mobile, uri);
        }
    }

    public void joinCommunity(String id) {
        List<String> joined = getJoinedCommunities();
        if (!joined.contains(id)) {
            joined.add(id);
            editor.putString(KEY_JOINED_COMMUNITIES, gson.toJson(joined)).apply();
        }
    }

    public void leaveCommunity(String id) {
        List<String> joined = getJoinedCommunities();
        if (joined.contains(id)) {
            joined.remove(id);
            editor.putString(KEY_JOINED_COMMUNITIES, gson.toJson(joined)).apply();
        }
    }

    public List<String> getJoinedCommunities() {
        String json = pref.getString(KEY_JOINED_COMMUNITIES, null);
        if (json == null) return new ArrayList<>();
        return gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
    }

    public boolean isCommunityJoined(String id) {
        return getJoinedCommunities().contains(id);
    }

    public static class LocalUser {
        public String name, email, password, mobile, profileImageUri;
        public LocalUser(String name, String email, String password, String mobile, String profileImageUri) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.mobile = mobile;
            this.profileImageUri = profileImageUri;
        }
    }
}

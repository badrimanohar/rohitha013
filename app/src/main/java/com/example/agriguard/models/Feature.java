package com.example.agriguard.models;

public class Feature {
    private String title;
    private String description;
    private int iconRes;
    private String buttonText;

    public Feature(String title, String description, int iconRes, String buttonText) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.buttonText = buttonText;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public String getButtonText() { return buttonText; }
}
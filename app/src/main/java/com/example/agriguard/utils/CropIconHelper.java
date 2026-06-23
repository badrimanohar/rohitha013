package com.example.agriguard.utils;

import com.example.agriguard.R;

public class CropIconHelper {
    public static int getCropIcon(String communityName) {
        String name = communityName.toLowerCase();
        
        if (name.contains("rice") || name.contains("paddy")) {
            return android.R.drawable.ic_menu_gallery; // Replace with R.drawable.ic_rice if available
        } else if (name.contains("wheat")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("maize") || name.contains("corn")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("apple")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("mango")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("grapes")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("tomato")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("potato")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("cotton")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("sugarcane")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("banana")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("orange") || name.contains("lemon")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("coconut")) {
            return android.R.drawable.ic_menu_gallery;
        } else if (name.contains("tea") || name.contains("coffee")) {
            return android.R.drawable.ic_menu_gallery;
        } else {
            return R.drawable.ic_launcher_foreground;
        }
    }
}

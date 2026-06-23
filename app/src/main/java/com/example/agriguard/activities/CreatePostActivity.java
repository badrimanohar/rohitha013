package com.example.agriguard.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.agriguard.R;
import com.google.android.material.appbar.MaterialToolbar;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_post).setOnClickListener(v -> {
            Toast.makeText(this, "Post shared to community!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

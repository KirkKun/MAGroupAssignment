package com.chenghakfan.magroupassignment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import my.edu.utar.mobileappass.R;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        ImageView btnBack = findViewById(R.id.btnBack);
        EditText etFeedback = findViewById(R.id.etFeedback);
        Button btnSubmit = findViewById(R.id.btnSubmitFeedback);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String feedback = etFeedback.getText().toString().trim();
            if (!feedback.isEmpty()) {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Please enter your thoughts first.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

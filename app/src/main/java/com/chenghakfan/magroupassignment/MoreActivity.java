package com.chenghakfan.magroupassignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MoreActivity extends AppCompatActivity {

    LinearLayout btnChatbot, btnFeedback, btnConverter, btnSetBudget, btnSavingsGoals, btnBillReminders;
    ImageView navHome, navWallet, navCharts, navMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        // Grid Items
        btnChatbot = findViewById(R.id.btnChatbot);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnConverter = findViewById(R.id.btnConverter);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnSavingsGoals = findViewById(R.id.btnSavingsGoals);
        btnBillReminders = findViewById(R.id.btnBillReminders);

        // Bottom Nav
        navHome = findViewById(R.id.navHome);
        navWallet = findViewById(R.id.navWallet);
        navCharts = findViewById(R.id.navCharts);
        navMore = findViewById(R.id.navMore);

        // Feature Listeners
        if (btnChatbot != null) btnChatbot.setOnClickListener(v -> startActivity(new Intent(this, ChatbotActivity.class)));
        if (btnFeedback != null) btnFeedback.setOnClickListener(v -> startActivity(new Intent(this, my.edu.utar.mobileappass.FeedbackActivity.class)));
        if (btnConverter != null) btnConverter.setOnClickListener(v -> startActivity(new Intent(this, my.edu.utar.mobileappass.ConverterActivity.class)));
        if (btnSetBudget != null) btnSetBudget.setOnClickListener(v -> startActivity(new Intent(this, SetBudgetActivity.class)));
        if (btnSavingsGoals != null) btnSavingsGoals.setOnClickListener(v -> startActivity(new Intent(this, SavingsGoalActivity.class)));
        if (btnBillReminders != null) btnBillReminders.setOnClickListener(v -> startActivity(new Intent(this, BillReminderActivity.class)));

        // Bottom Navigation Listeners
        navHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        navWallet.setOnClickListener(v -> startActivity(new Intent(this, WalletActivity.class)));
        navCharts.setOnClickListener(v -> startActivity(new Intent(this, ChartsActivity.class)));
    }
}

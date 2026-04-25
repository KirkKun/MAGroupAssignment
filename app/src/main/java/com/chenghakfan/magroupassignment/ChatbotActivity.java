package com.chenghakfan.magroupassignment;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import my.edu.utar.mobileappass.R;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageView btnSend, btnBack;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatList;
    private DatabaseHelper db;
    private LinearLayout layoutSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        db = new DatabaseHelper(this);
        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        layoutSuggestions = findViewById(R.id.layoutSuggestions);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        addAiMessage("👋 Hello! I'm your AI Financial Assistant.\n\nI can help you track your budget, analyze your spending, and provide personalized saving advice. What would you like to know?");

        btnSend.setOnClickListener(v -> sendMessage());

        setupSuggestions();
    }

    private void setupSuggestions() {
        for (int i = 0; i < layoutSuggestions.getChildCount(); i++) {
            View v = layoutSuggestions.getChildAt(i);
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setOnClickListener(view -> {
                    String query = tv.getText().toString();
                    addUserMessage(query);
                    processAiResponse(query);
                });
            }
        }
    }

    private void sendMessage() {
        String query = etMessage.getText().toString().trim();
        if (!query.isEmpty()) {
            addUserMessage(query);
            etMessage.setText("");
            processAiResponse(query);
        }
    }

    private void addUserMessage(String message) {
        chatList.add(new ChatMessage(message, true));
        adapter.notifyItemInserted(chatList.size() - 1);
        recyclerChat.smoothScrollToPosition(chatList.size() - 1);
    }

    private void addAiMessage(String message) {
        chatList.add(new ChatMessage(message, false));
        adapter.notifyItemInserted(chatList.size() - 1);
        recyclerChat.smoothScrollToPosition(chatList.size() - 1);
    }

    private void processAiResponse(String query) {
        String lowerQuery = query.toLowerCase();
        Calendar cal = Calendar.getInstance();
        String monthKey = String.format(Locale.getDefault(), "%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

        new Handler().postDelayed(() -> {
            if (lowerQuery.contains("budget") || lowerQuery.contains("limit")) {
                double budget = db.getMonthlyBudget(monthKey);
                if (budget > 0) {
                    addAiMessage("💰 Your budget for " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " is RM" + String.format("%.2f", budget) + ".");
                } else {
                    addAiMessage("❓ You haven't set a budget for this month. Setting a budget is the first step to financial freedom! You can do this in the 'Set Budget' section.");
                }

            } else if (lowerQuery.contains("expense") || lowerQuery.contains("spent") || lowerQuery.contains("transaction")) {
                double spent = db.getTotalExpensesForMonth(monthKey);
                addAiMessage("💸 You have spent RM" + String.format("%.2f", spent) + " so far this month.");

            } else if (lowerQuery.contains("advice") || lowerQuery.contains("help") || lowerQuery.contains("saving tips")) {
                addAiMessage(AiHelper.generateRecommendation(db, monthKey));

            } else if (lowerQuery.contains("health") || lowerQuery.contains("score")) {
                int score = AiHelper.calculateFinancialHealthScore(db, monthKey);
                String msg = "🏥 Financial Health Score: " + score + "/100\n\n";
                if (score >= 80) msg += "🌟 Outstanding! You're in total control of your money.";
                else if (score >= 60) msg += "👍 Good progress. A few small adjustments could make it perfect!";
                else msg += "⚠️ There's room for improvement. Try reducing non-essential expenses this week.";
                addAiMessage(msg);

            } else if (lowerQuery.contains("goal") || lowerQuery.contains("saving")) {
                addAiMessage(AiHelper.getSavingsSummary(db));

            } else if (lowerQuery.contains("bill") || lowerQuery.contains("due") || lowerQuery.contains("reminder")) {
                addAiMessage(AiHelper.getUpcomingBills(db));

            } else if (lowerQuery.contains("forecast") || lowerQuery.contains("predict") || lowerQuery.contains("will i overspend")) {
                addAiMessage(AiHelper.predictOverspending(db, monthKey));

            } else if (lowerQuery.contains("hello") || lowerQuery.contains("hi")) {
                addAiMessage("Hi there! Ready to take charge of your finances today? Try asking about your 'budget' or 'savings goals'!");

            } else {
                addAiMessage("🤖 I'm still learning! You can ask me about:\n\n" +
                        "• Budget & Expenses\n" +
                        "• Savings Goals\n" +
                        "• Upcoming Bills\n" +
                        "• Financial Health Score\n" +
                        "• Spending Forecast");
            }
        }, 800);
    }
}

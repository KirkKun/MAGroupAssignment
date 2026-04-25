package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView tvMonth, tvIncome, tvExpense, tvPrediction, tvBudgetUsage, tvHealthScore;
    private TextView btnPrevMonth, btnNextMonth;
    private ProgressBar progressBudget;
    private RecyclerView recyclerTransactions;
    private ImageView navHome, navWallet, navCharts, navMore;
    private DatabaseHelper db;

    private int selectedYear;
    private int selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = new DatabaseHelper(this);

        tvMonth = findViewById(R.id.tvMonth);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvPrediction = findViewById(R.id.tvPrediction);
        tvBudgetUsage = findViewById(R.id.tvBudgetUsage);
        tvHealthScore = findViewById(R.id.tvHealthScore);
        progressBudget = findViewById(R.id.progressBudget);
        recyclerTransactions = findViewById(R.id.recyclerTransactions);

        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        navHome = findViewById(R.id.navHome);
        navWallet = findViewById(R.id.navWallet);
        navCharts = findViewById(R.id.navCharts);
        navMore = findViewById(R.id.navMore);

        recyclerTransactions.setLayoutManager(new LinearLayoutManager(this));

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);

        btnPrevMonth.setOnClickListener(v -> {
            selectedMonth--;
            if (selectedMonth < 0) {
                selectedMonth = 11;
                selectedYear--;
            }
            loadDashboard();
        });

        btnNextMonth.setOnClickListener(v -> {
            selectedMonth++;
            if (selectedMonth > 11) {
                selectedMonth = 0;
                selectedYear++;
            }
            loadDashboard();
        });

        navWallet.setOnClickListener(v ->
                startActivity(new Intent(com.chenghakfan.magroupassignment.HomeActivity.this, WalletActivity.class)));

        navCharts.setOnClickListener(v ->
                startActivity(new Intent(com.chenghakfan.magroupassignment.HomeActivity.this, ChartsActivity.class)));

        navMore.setOnClickListener(v ->
                startActivity(new Intent(com.chenghakfan.magroupassignment.HomeActivity.this, MoreActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
        checkGoalsMet();
    }

    private void loadDashboard() {
        String monthKey = String.format(Locale.getDefault(), "%04d-%02d", selectedYear, selectedMonth + 1);
        String monthDisplay = new DateFormatSymbols().getMonths()[selectedMonth] + " " + selectedYear;
        tvMonth.setText(monthDisplay);

        double income = db.getTotalIncomeForMonth(monthKey);
        double expense = db.getTotalExpensesForMonth(monthKey);
        double budget = db.getMonthlyBudget(monthKey);

        ArrayList<ExpenseModel> list = db.getTransactionsByMonth(monthKey);
        ExpenseAdapter adapter = new ExpenseAdapter(this, list);
        recyclerTransactions.setAdapter(adapter);

        tvIncome.setText("RM" + String.format(Locale.getDefault(), "%.2f", income));
        tvExpense.setText("RM" + String.format(Locale.getDefault(), "%.2f", expense));

        if (budget > 0) {
            int usage = (int) ((expense / budget) * 100);
            if (usage > 100) usage = 100;
            progressBudget.setProgress(usage);
            tvBudgetUsage.setText("Budget Used: " + usage + "%");
        } else {
            progressBudget.setProgress(0);
            tvBudgetUsage.setText("Set a budget to track progress");
        }

        tvPrediction.setText(AiHelper.predictOverspending(db, monthKey));
        tvHealthScore.setText(String.valueOf(AiHelper.calculateFinancialHealthScore(db, monthKey)));
    }

    private void checkGoalsMet() {
        double totalAssets = (db.getTotalIncome() - db.getTotalExpenses()) + db.getTotalAssetsValue();
        ArrayList<SavingsGoalModel> goals = db.getAllSavingsGoals();

        for (SavingsGoalModel goal : goals) {
            if (totalAssets >= goal.getTargetAmount()) {
                new AlertDialog.Builder(this)
                        .setTitle("Goal Achieved! 🎉")
                        .setMessage("Congratulations! You've reached your goal: " + goal.getTitle() +
                                " (RM" + String.format("%.2f", goal.getTargetAmount()) + "). The goal has been completed and removed from your list.")
                        .setPositiveButton("Awesome", (dialog, which) -> {
                            db.deleteSavingsGoal(goal.getId());
                        })
                        .show();
            }
        }
    }
}

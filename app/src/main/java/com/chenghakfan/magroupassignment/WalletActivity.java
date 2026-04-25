package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WalletActivity extends AppCompatActivity {

    TextView tvNetWorth, tvAssets, tvDebt, tvAccountBalance, tvUSD, tvGoalsTotal, tvManualAssets;
    Button btnSetBudget, btnAddExpense;
    ImageView navHome, navWallet, navCharts, navMore;
    LinearLayout layoutAssets;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        db = new DatabaseHelper(this);

        tvNetWorth = findViewById(R.id.tvNetWorth);
        tvAssets = findViewById(R.id.tvAssets);
        tvDebt = findViewById(R.id.tvDebt);
        tvAccountBalance = findViewById(R.id.tvAccountBalance);
        tvUSD = findViewById(R.id.tvUSD);
        tvGoalsTotal = findViewById(R.id.tvGoalsTotal);
        tvManualAssets = findViewById(R.id.tvManualAssets);

        layoutAssets = findViewById(R.id.layoutAssets);

        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnAddExpense = findViewById(R.id.btnAddExpense);

        navHome = findViewById(R.id.navHome);
        navWallet = findViewById(R.id.navWallet);
        navCharts = findViewById(R.id.navCharts);
        navMore = findViewById(R.id.navMore);

        btnSetBudget.setOnClickListener(v -> startActivity(new Intent(this, SetBudgetActivity.class)));
        btnAddExpense.setOnClickListener(v -> startActivity(new Intent(this, AddExpenseActivity.class)));

        layoutAssets.setOnClickListener(v -> showAddAssetDialog());

        navHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        navCharts.setOnClickListener(v -> startActivity(new Intent(this, ChartsActivity.class)));
        navMore.setOnClickListener(v -> startActivity(new Intent(this, MoreActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWallet();
    }

    private void loadWallet() {
        double totalIncome = db.getTotalIncome();
        double totalExpense = db.getTotalExpenses();

        // Calculate Savings Goals Total
        double goalsTotal = 0;
        ArrayList<SavingsGoalModel> goals = db.getAllSavingsGoals();
        for (SavingsGoalModel goal : goals) {
            goalsTotal += goal.getCurrentAmount();
        }

        // Manual Assets Total
        double manualAssetsTotal = db.getTotalAssetsValue();

        // Calculate unpaid bills as Debt
        double debt = 0;

        double currentBalance = totalIncome - totalExpense;
        double totalAssets = currentBalance + goalsTotal + manualAssetsTotal;
        double netWorth = totalAssets - debt;

        tvAssets.setText(String.format(Locale.getDefault(), "RM%.2f", totalAssets));
        tvDebt.setText(String.format(Locale.getDefault(), "RM%.2f", debt));
        tvNetWorth.setText(String.format(Locale.getDefault(), "RM%.2f", netWorth));
        tvAccountBalance.setText(String.format(Locale.getDefault(), "RM%.2f", currentBalance));
        tvGoalsTotal.setText(String.format(Locale.getDefault(), "RM%.2f", goalsTotal));
        tvManualAssets.setText(String.format(Locale.getDefault(), "RM%.2f", manualAssetsTotal));

        fetchExchangeRate(netWorth);
    }

    private void showAddAssetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Asset");
        builder.setMessage("Enter manual asset value (e.g. Property, Stock, Gold)");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_goal, null); // Reuse the same layout for simplicity
        EditText etName = view.findViewById(R.id.etGoalTitle);
        etName.setHint("Asset Name (e.g. Stock Investment)");
        EditText etValue = view.findViewById(R.id.etGoalTarget);
        etValue.setHint("Current Value (RM)");

        builder.setView(view);

        builder.setPositiveButton("Add Asset", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String valStr = etValue.getText().toString().trim();
            if (!name.isEmpty() && !valStr.isEmpty()) {
                double value = Double.parseDouble(valStr);
                if (db.addAsset(name, value, "Manual")) {
                    Toast.makeText(this, "Asset added!", Toast.LENGTH_SHORT).show();
                    loadWallet();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void fetchExchangeRate(double myrAmount) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v6.exchangerate-api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CurrencyService service = retrofit.create(CurrencyService.class);
        service.getLatestRates("MYR").enqueue(new Callback<CurrencyResponse>() {
            @Override
            public void onResponse(Call<CurrencyResponse> call, Response<CurrencyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Double usdRate = response.body().conversion_rates.get("USD");
                    if (usdRate != null) {
                        double usdAmount = myrAmount * usdRate;
                        tvUSD.setText(String.format(Locale.getDefault(), "≈ $%.2f USD", usdAmount));
                    }
                } else {
                    tvUSD.setText("Rate Unavailable");
                }
            }

            @Override
            public void onFailure(Call<CurrencyResponse> call, Throwable t) {
                tvUSD.setText("Offline");
            }
        });
    }
}
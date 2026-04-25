package com.chenghakfan.magroupassignment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class SetBudgetActivity extends AppCompatActivity {

    EditText etMonth, etAmount;
    Button btnSaveBudget;
    ImageView btnBack;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        db = new DatabaseHelper(this);

        etMonth = findViewById(R.id.etMonth);
        etAmount = findViewById(R.id.etAmount);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnBack = findViewById(R.id.btnBack);

        Calendar calendar = Calendar.getInstance();
        etMonth.setText(String.format(Locale.getDefault(), "%04d-%02d",
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1));

        etMonth.setOnClickListener(v -> showMonthPicker());

        btnBack.setOnClickListener(v -> finish());

        btnSaveBudget.setOnClickListener(v -> {
            String month = etMonth.getText().toString().trim();
            String amountText = etAmount.getText().toString().trim();

            if (month.isEmpty() || amountText.isEmpty()) {
                Toast.makeText(this, "Please enter month and amount", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                boolean success = db.setMonthlyBudget(month, amount);

                if (success) {
                    Toast.makeText(this, "Budget saved for " + month, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMonthPicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etMonth.setText(String.format(Locale.getDefault(), "%04d-%02d", year, month + 1));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setTitle("Select Month");
        datePickerDialog.show();
    }
}

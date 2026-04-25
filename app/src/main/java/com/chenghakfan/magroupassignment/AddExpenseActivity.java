package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    EditText etTitle, etAmount, etDate;
    Spinner spinnerCategory, spinnerType;
    Button btnSave, btnAddCategory;
    ImageView btnBack;
    LinearLayout layoutDate;
    DatabaseHelper db;

    boolean isEdit = false;
    int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        db = new DatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerType = findViewById(R.id.spinnerType);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        layoutDate = findViewById(R.id.layoutDate);

        setupSpinners();

        if (getIntent().hasExtra("isEdit")) {
            isEdit = true;
            transactionId = getIntent().getIntExtra("id", -1);
            etTitle.setText(getIntent().getStringExtra("title"));
            etAmount.setText(String.valueOf(getIntent().getDoubleExtra("amount", 0)));
            etDate.setText(getIntent().getStringExtra("date"));
            btnSave.setText("Update Transaction");

            String type = getIntent().getStringExtra("type");
            if (type != null && type.equalsIgnoreCase("income")) {
                spinnerType.setSelection(0);
            } else {
                spinnerType.setSelection(1);
            }
        }

        btnBack.setOnClickListener(v -> finish());
        layoutDate.setOnClickListener(v -> showDatePicker());
        etDate.setOnClickListener(v -> showDatePicker());

        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupSpinners() {
        ArrayList<String> categories = db.getCategories();
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] types = {"Income", "Expense"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");

        EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                if (db.addCategory(name)) {
                    setupSpinners();
                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveTransaction() {
        String title = etTitle.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString().trim() : "";
        String typeDisplay = spinnerType.getSelectedItem() != null ? spinnerType.getSelectedItem().toString().trim() : "Expense";
        String date = etDate.getText().toString().trim();
        String amountText = etAmount.getText().toString().trim();

        if (title.isEmpty() || amountText.isEmpty() || date.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = typeDisplay.equalsIgnoreCase("Income") ? "income" : "expense";

        boolean success;
        if (isEdit) {
            success = db.updateTransaction(transactionId, title, category, amount, date, type);
        } else {
            success = db.addTransaction(title, category, amount, date, type);
        }

        if (success) {
            Toast.makeText(this, isEdit ? "Updated" : "Saved", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, day) -> {
            etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}

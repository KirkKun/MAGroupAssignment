package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DebtTrackerActivity extends AppCompatActivity {

    private RecyclerView recyclerDebts;
    private DebtAdapter adapter;
    private ArrayList<DebtModel> debtList;
    private DatabaseHelper db;
    private FloatingActionButton fabAddDebt;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_tracker);

        db = new DatabaseHelper(this);
        recyclerDebts = findViewById(R.id.recyclerDebts);
        fabAddDebt = findViewById(R.id.fabAddDebt);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        loadDebts();

        fabAddDebt.setOnClickListener(v -> showAddDebtDialog());
    }

    private void loadDebts() {
        debtList = db.getAllDebts();
        adapter = new DebtAdapter(debtList, db);
        recyclerDebts.setLayoutManager(new LinearLayoutManager(this));
        recyclerDebts.setAdapter(adapter);
    }

    private void showAddDebtDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Debt");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_debt, null);
        EditText etTitle = view.findViewById(R.id.etDebtTitle);
        EditText etAmount = view.findViewById(R.id.etDebtAmount);
        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (!title.isEmpty() && !amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                if (db.addDebt(title, amount)) {
                    loadDebts();
                    Toast.makeText(this, "Debt added!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}

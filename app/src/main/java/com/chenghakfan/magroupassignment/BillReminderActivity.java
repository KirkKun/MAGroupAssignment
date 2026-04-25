package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import my.edu.utar.mobileappass.R;

public class BillReminderActivity extends AppCompatActivity implements BillReminderAdapter.OnBillActionListener {

    private DatabaseHelper db;
    private RecyclerView recyclerBills;
    private Button btnAddBill;
    private ImageView btnBack;
    private BillReminderAdapter adapter;
    private ArrayList<BillReminderModel> billList;

    private static boolean hasAlertedThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_reminders);

        db = new DatabaseHelper(this);
        recyclerBills = findViewById(R.id.recyclerBills);
        btnAddBill = findViewById(R.id.btnAddBill);
        btnBack = findViewById(R.id.btnBack);

        recyclerBills.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());

        btnAddBill.setOnClickListener(v -> showAddBillDialog(null));

        loadBills();

        if (!hasAlertedThisSession) {
            checkOverdueBills();
            hasAlertedThisSession = true;
        }
    }

    private void loadBills() {
        billList = new ArrayList<>();
        android.database.Cursor cursor = db.getBillReminders();
        if (cursor.moveToFirst()) {
            do {
                billList.add(new BillReminderModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getString(3),
                        cursor.getInt(4)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new BillReminderAdapter(this, billList, this);
        recyclerBills.setAdapter(adapter);
    }

    private void checkOverdueBills() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        StringBuilder overdueBills = new StringBuilder();

        for (BillReminderModel bill : billList) {
            if (bill.getDueDate().compareTo(today) <= 0) {
                if (overdueBills.length() > 0) overdueBills.append("\n");
                overdueBills.append("• ").append(bill.getTitle()).append(" (RM").append(String.format("%.2f", bill.getAmount())).append(")");
            }
        }

        if (overdueBills.length() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Upcoming/Overdue Bills 🔔")
                    .setMessage("You have bills due:\n\n" + overdueBills.toString())
                    .setPositiveButton("Got it", null)
                    .show();
        }
    }

    private void showAddBillDialog(BillReminderModel existingBill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingBill == null ? "Add Bill Alert" : "Edit Bill Alert");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_bill, null);
        EditText etTitle = view.findViewById(R.id.etBillTitle);
        EditText etAmount = view.findViewById(R.id.etBillAmount);
        EditText etDate = view.findViewById(R.id.etBillDate);

        if (existingBill != null) {
            etTitle.setText(existingBill.getTitle());
            etAmount.setText(String.valueOf(existingBill.getAmount()));
            etDate.setText(existingBill.getDueDate());
        }

        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                etDate.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(view);
        builder.setPositiveButton(existingBill == null ? "Set Alert" : "Update", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (!title.isEmpty() && !amountStr.isEmpty() && !date.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    boolean success;
                    if (existingBill == null) {
                        success = db.addBillReminder(title, amount, date);
                    } else {
                        success = db.updateBillReminder(existingBill.getId(), title, amount, date);
                    }

                    if (success) {
                        Toast.makeText(this, "Reminder saved!", Toast.LENGTH_SHORT).show();
                        loadBills();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onEdit(BillReminderModel model) {
        showAddBillDialog(model);
    }

    @Override
    public void onDelete(BillReminderModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill Alert")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (db.deleteBillReminder(model.getId())) {
                        loadBills();
                        Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

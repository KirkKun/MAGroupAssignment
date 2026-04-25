package com.chenghakfan.magroupassignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "financial_wellness.db";
    private static final int DB_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE budget (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "month TEXT, " +
                "amount REAL)");

        db.execSQL("CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " +
                "amount REAL, " +
                "date TEXT, " +
                "type TEXT)");

        db.execSQL("CREATE TABLE categories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE)");

        db.execSQL("CREATE TABLE savings_goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "target_amount REAL, " +
                "current_amount REAL)");

        db.execSQL("CREATE TABLE bill_reminders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "amount REAL, " +
                "due_date TEXT, " +
                "status INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE assets (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "value REAL, " +
                "type TEXT)");

        String[] defaultCategories = {"Food", "Transport", "Entertainment", "Education", "Health", "Others"};
        for (String cat : defaultCategories) {
            db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES ('" + cat + "')");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE)");
            String[] defaultCategories = {"Food", "Transport", "Entertainment", "Education", "Health", "Others"};
            for (String cat : defaultCategories) {
                db.execSQL("INSERT OR IGNORE INTO categories (name) VALUES ('" + cat + "')");
            }
        }
        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "target_amount REAL, " +
                    "current_amount REAL)");
            db.execSQL("CREATE TABLE IF NOT EXISTS bill_reminders (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "amount REAL, " +
                    "due_date TEXT, " +
                    "status INTEGER DEFAULT 0)");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS assets (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "value REAL, " +
                    "type TEXT)");
        }
    }

    public boolean addTransaction(String title, String category, double amount, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("category", category);
        cv.put("amount", amount);
        cv.put("date", date);
        cv.put("type", type);
        return db.insert("transactions", null, cv) != -1;
    }

    public boolean updateTransaction(int id, String title, String category, double amount, String date, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("category", category);
        cv.put("amount", amount);
        cv.put("date", date);
        cv.put("type", type);
        return db.update("transactions", cv, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("transactions", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public ArrayList<ExpenseModel> getTransactionsByMonth(String monthPrefix) {
        ArrayList<ExpenseModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE date LIKE ? ORDER BY date DESC, id DESC", new String[]{monthPrefix + "%"});
        if (cursor.moveToFirst()) {
            do {
                list.add(new ExpenseModel(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getDouble(3), cursor.getString(4), cursor.getString(5)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean setMonthlyBudget(String month, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("month", month);
        cv.put("amount", amount);
        Cursor cursor = db.rawQuery("SELECT * FROM budget WHERE month=?", new String[]{month});
        long res;
        if (cursor.moveToFirst()) {
            res = db.update("budget", cv, "month=?", new String[]{month});
        } else {
            res = db.insert("budget", null, cv);
        }
        cursor.close();
        return res != -1;
    }

    public double getMonthlyBudget(String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT amount FROM budget WHERE month=?", new String[]{month});
        double amount = 0;
        if (cursor.moveToFirst()) amount = cursor.getDouble(0);
        cursor.close();
        return amount;
    }

    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type='income'", null);
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type='expense'", null);
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public double getTotalIncomeForMonth(String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type='income' AND date LIKE ?", new String[]{monthPrefix + "%"});
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public double getTotalExpensesForMonth(String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type='expense' AND date LIKE ?", new String[]{monthPrefix + "%"});
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public double getCategoryExpenseForMonth(String category, String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0) FROM transactions WHERE type='expense' AND category=? AND date LIKE ?", new String[]{category, monthPrefix + "%"});
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public double getAverageDailyExpenseForMonth(String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(amount), 0), COUNT(DISTINCT date) FROM transactions WHERE type='expense' AND date LIKE ?", new String[]{monthPrefix + "%"});
        double total = 0; int days = 0;
        if (cursor.moveToFirst()) { total = cursor.getDouble(0); days = cursor.getInt(1); }
        cursor.close();
        return days == 0 ? 0 : total / days;
    }

    public int getActiveDaysCountForMonth(String monthPrefix) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT date) FROM transactions WHERE date LIKE ?", new String[]{monthPrefix + "%"});
        int res = 0; if (cursor.moveToFirst()) res = cursor.getInt(0);
        cursor.close();
        return res;
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories", null);
        if (cursor.moveToFirst()) { do { list.add(cursor.getString(0)); } while (cursor.moveToNext()); }
        cursor.close();
        return list;
    }

    public boolean addCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        return db.insert("categories", null, cv) != -1;
    }

    public boolean addSavingsGoal(String title, double target) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("target_amount", target);
        cv.put("current_amount", 0);
        return db.insert("savings_goals", null, cv) != -1;
    }

    public boolean updateSavingsGoalProgress(int id, double currentAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("current_amount", currentAmount);
        return db.update("savings_goals", cv, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteSavingsGoal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("savings_goals", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public ArrayList<SavingsGoalModel> getAllSavingsGoals() {
        ArrayList<SavingsGoalModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM savings_goals", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new SavingsGoalModel(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getDouble(2),
                        cursor.getDouble(3)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public double getSavingsGoalsTotal() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(current_amount), 0) FROM savings_goals", null);
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }

    public boolean addBillReminder(String title, double amount, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("amount", amount);
        cv.put("due_date", dueDate);
        return db.insert("bill_reminders", null, cv) != -1;
    }

    public boolean updateBillReminder(int id, String title, double amount, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("amount", amount);
        cv.put("due_date", dueDate);
        return db.update("bill_reminders", cv, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteBillReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("bill_reminders", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getBillReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM bill_reminders ORDER BY due_date ASC", null);
    }

    public boolean addAsset(String name, double value, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("value", value);
        cv.put("type", type);
        return db.insert("assets", null, cv) != -1;
    }

    public double getTotalAssetsValue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT IFNULL(SUM(value), 0) FROM assets", null);
        double res = 0;
        if (cursor.moveToFirst()) res = cursor.getDouble(0);
        cursor.close();
        return res;
    }
}

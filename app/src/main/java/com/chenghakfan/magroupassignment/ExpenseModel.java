package com.chenghakfan.magroupassignment;

public class ExpenseModel {
    int id;
    String title;
    String category;
    double amount;
    String date;
    String type;

    public ExpenseModel(int id, String title, String category, double amount, String date, String type) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getType() { return type; }
}
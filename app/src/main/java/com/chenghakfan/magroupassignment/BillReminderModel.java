package com.chenghakfan.magroupassignment;

public class BillReminderModel {
    private int id;
    private String title;
    private double amount;
    private String dueDate;
    private int status; // 0 for unpaid, 1 for paid

    public BillReminderModel(int id, String title, double amount, String dueDate, int status) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = status;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getAmount() { return amount; }
    public String getDueDate() { return dueDate; }
    public int getStatus() { return status; }
}

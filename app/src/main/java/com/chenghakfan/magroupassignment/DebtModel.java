package com.chenghakfan.magroupassignment;

public class DebtModel {
    private int id;
    private String title;
    private double totalAmount;
    private double amountPaid;

    public DebtModel(int id, String title, double totalAmount, double amountPaid) {
        this.id = id;
        this.title = title;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getTotalAmount() { return totalAmount; }
    public double getAmountPaid() { return amountPaid; }
}

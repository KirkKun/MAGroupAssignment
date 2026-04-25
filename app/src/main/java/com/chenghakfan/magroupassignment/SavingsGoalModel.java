package com.chenghakfan.magroupassignment;

public class SavingsGoalModel {
    private int id;
    private String title;
    private double targetAmount;
    private double currentAmount;

    public SavingsGoalModel(int id, String title, double targetAmount, double currentAmount) {
        this.id = id;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
}

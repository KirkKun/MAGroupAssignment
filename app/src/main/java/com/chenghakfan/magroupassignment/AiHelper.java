package com.chenghakfan.magroupassignment;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AiHelper {

    public static String generateRecommendation(DatabaseHelper db, String month) {
        double food = db.getCategoryExpenseForMonth("Food", month);
        double transport = db.getCategoryExpenseForMonth("Transport", month);
        double entertainment = db.getCategoryExpenseForMonth("Entertainment", month);
        double education = db.getCategoryExpenseForMonth("Education", month);
        double others = db.getCategoryExpenseForMonth("Others", month);

        double totalExpense = db.getTotalExpensesForMonth(month);
        double budget = db.getMonthlyBudget(month);

        if (totalExpense == 0 && budget == 0) {
            return "Start tracking your expenses to get personalized advice!";
        }

        StringBuilder advice = new StringBuilder();

        if (budget > 0) {
            double usage = (totalExpense / budget) * 100;

            if (usage >= 90) {
                advice.append("⚠️ CRITICAL: You've used ").append(String.format("%.1f", usage)).append("% of your budget. I strongly suggest pausing non-essential spending immediately.");
            } else if (usage >= 70) {
                advice.append("⚠️ CAUTION: You're at ").append(String.format("%.1f", usage)).append("% of your budget. Try to stay within your limits for the rest of the month.");
            } else {
                advice.append("✅ GOOD: You've used ").append(String.format("%.1f", usage)).append("% of your budget. You're managing your finances well!");
            }
        } else {
            advice.append("💡 Pro Tip: Set a monthly budget in the 'Set Budget' section to get better spending insights.");
        }

        double highest = Math.max(food, Math.max(transport, Math.max(entertainment, Math.max(education, others))));
        if (highest > 0) {
            advice.append("\n\n📊 Spending Insight:\n");
            if (highest == food) {
                advice.append("Your highest spending is on Food (RM").append(String.format("%.2f", food)).append("). Consider home-cooked meals to save more!");
            } else if (highest == transport) {
                advice.append("Transport is your top expense (RM").append(String.format("%.2f", transport)).append("). Check if public transport or carpooling is an option.");
            } else if (highest == entertainment) {
                advice.append("Entertainment costs are quite high (RM").append(String.format("%.2f", entertainment)).append("). Maybe look for free weekend activities?");
            } else if (highest == education) {
                advice.append("Education is your primary investment (RM").append(String.format("%.2f", education)).append("). This is a great way to use your funds!");
            } else {
                advice.append("Miscellaneous spending is your highest category (RM").append(String.format("%.2f", others)).append("). Try to track these more specifically.");
            }
        }

        return advice.toString();
    }

    public static String predictOverspending(DatabaseHelper db, String month) {
        double budget = db.getMonthlyBudget(month);
        double spent = db.getTotalExpensesForMonth(month);
        double avgDaily = db.getAverageDailyExpenseForMonth(month);

        if (budget <= 0) {
            return "I can't predict overspending without a budget. Set one up to see your forecast!";
        }

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int remainingDays = maxDay - currentDay;

        double predictedTotal = spent + (avgDaily * remainingDays);
        double predictedBalance = budget - predictedTotal;

        if (predictedTotal > budget) {
            return "🔮 Forecast: Based on your current spending rate, you might exceed your budget by RM" + String.format("%.2f", Math.abs(predictedBalance)) + " by the end of the month.";
        } else {
            return "🔮 Forecast: You're doing great! You are on track to stay within your budget with an estimated RM" + String.format("%.2f", predictedBalance) + " left over.";
        }
    }

    public static int calculateFinancialHealthScore(DatabaseHelper db, String month) {
        double budget = db.getMonthlyBudget(month);
        double expenses = db.getTotalExpensesForMonth(month);
        double income = db.getTotalIncomeForMonth(month);

        if (budget <= 0 && expenses <= 0 && income <= 0) return 0;

        int score = 70; // Baseline

        // 1. Budget Compliance
        if (budget > 0) {
            double usage = (expenses / budget) * 100;
            if (usage > 100) score -= 30;
            else if (usage > 80) score -= 10;
            else if (usage < 50) score += 10;
        }

        // 2. Savings Rate
        if (income > 0) {
            double savingsRate = ((income - expenses) / income) * 100;
            if (savingsRate >= 20) score += 20;
            else if (savingsRate < 0) score -= 20;
        }

        return Math.min(100, Math.max(0, score));
    }

    public static String getSavingsSummary(DatabaseHelper db) {
        ArrayList<SavingsGoalModel> goals = db.getAllSavingsGoals();
        if (goals.isEmpty()) return "You haven't set any savings goals yet. Start one to build your wealth!";

        StringBuilder sb = new StringBuilder("🎯 Your Savings Goals:\n");
        for (SavingsGoalModel goal : goals) {
            double progress = (goal.getCurrentAmount() / goal.getTargetAmount()) * 100;
            sb.append("- ").append(goal.getTitle())
                    .append(": ").append(String.format("%.1f", progress)).append("% reached ")
                    .append("(RM").append(String.format("%.2f", goal.getCurrentAmount()))
                    .append("/RM").append(String.format("%.2f", goal.getTargetAmount())).append(")\n");
        }
        return sb.toString();
    }

    public static String getUpcomingBills(DatabaseHelper db) {
        Cursor cursor = db.getBillReminders();
        if (cursor == null || cursor.getCount() == 0) return "You have no upcoming bills or reminders. Nice!";

        StringBuilder sb = new StringBuilder("📅 Upcoming Bills:\n");
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                sb.append("- ").append(cursor.getString(1)) // title
                        .append(" (RM").append(String.format("%.2f", cursor.getDouble(2))).append(") ")
                        .append("due on ").append(cursor.getString(3)).append("\n");
                count++;
                if (count >= 5) break; // Limit to 5
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sb.toString();
    }
}

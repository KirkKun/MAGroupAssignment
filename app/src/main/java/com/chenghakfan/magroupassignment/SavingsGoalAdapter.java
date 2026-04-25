package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SavingsGoalModel> list;
    private DatabaseHelper db;

    public SavingsGoalAdapter(Context context, ArrayList<SavingsGoalModel> list) {
        this.context = context;
        this.list = list;
        this.db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingsGoalModel model = list.get(position);

        holder.tvTitle.setText(model.getTitle());
        holder.tvProgress.setText(String.format(Locale.getDefault(), "RM%.2f / RM%.2f", model.getCurrentAmount(), model.getTargetAmount()));

        int progress = (int) ((model.getCurrentAmount() / model.getTargetAmount()) * 100);
        holder.progressBar.setProgress(Math.min(progress, 100));

        holder.btnAddMoney.setOnClickListener(v -> {
            showAddMoneyDialog(model, position);
        });
    }

    private void showAddMoneyDialog(SavingsGoalModel model, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Savings for " + model.getTitle());

        EditText input = new EditText(context);
        input.setHint("Enter amount (RM)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                double addedAmount = Double.parseDouble(amountStr);
                double newTotal = model.getCurrentAmount() + addedAmount;
                
                if (db.updateSavingsGoalProgress(model.getId(), newTotal)) {
                    model.setCurrentAmount(newTotal);
                    notifyItemChanged(position);

                    // --- CELEBRATION LOGIC ---
                    if (newTotal >= model.getTargetAmount()) {
                        showCelebrationDialog(model.getTitle());
                    } else {
                        Toast.makeText(context, "Progress updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCelebrationDialog(String goalTitle) {
        new AlertDialog.Builder(context)
                .setTitle("🎉 Congratulations!")
                .setMessage("Fantastic news! You have reached your savings goal for '" + goalTitle + "'! \n\nYour financial discipline is truly inspiring. Keep up the great work!")
                .setPositiveButton("Awesome!", null)
                .setIcon(android.R.drawable.btn_star_big_on)
                .show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgress;
        ProgressBar progressBar;
        Button btnAddMoney;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGoalTitle);
            tvProgress = itemView.findViewById(R.id.tvGoalProgressText);
            progressBar = itemView.findViewById(R.id.progressGoal);
            btnAddMoney = itemView.findViewById(R.id.btnAddMoney);
        }
    }
}

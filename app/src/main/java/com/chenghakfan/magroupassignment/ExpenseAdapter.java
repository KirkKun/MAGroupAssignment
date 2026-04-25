package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    Context context;
    ArrayList<ExpenseModel> list;
    DatabaseHelper db;

    public ExpenseAdapter(Context context, ArrayList<ExpenseModel> list) {
        this.context = context;
        this.list = list;
        this.db = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseModel model = list.get(position);

        holder.tvDate.setText(model.getDate());
        holder.tvTitle.setText(model.getTitle());
        holder.tvCategory.setText(model.getCategory());

        if (model.getType().equalsIgnoreCase("income")) {
            holder.tvAmount.setText("+ RM" + String.format(Locale.getDefault(), "%.2f", model.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvAmount.setText("- RM" + String.format(Locale.getDefault(), "%.2f", model.getAmount()));
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"));
        }

        String category = model.getCategory();

        // Icon logic
        if (category.equalsIgnoreCase("Food")) {
            holder.imgIcon.setImageResource(R.drawable.restaurant);
        } else if (category.equalsIgnoreCase("Transport")) {
            holder.imgIcon.setImageResource(R.drawable.car);
        } else if (category.equalsIgnoreCase("Entertainment")) {
            holder.imgIcon.setImageResource(R.drawable.cinema);
        } else if (category.equalsIgnoreCase("Education")) {
            holder.imgIcon.setImageResource(R.drawable.school);
        } else if (category.equalsIgnoreCase("Income")) {
            holder.imgIcon.setImageResource(R.drawable.profit);
        } else {
            holder.imgIcon.setImageResource(R.drawable.application);
        }

        // Long click to Delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (db.deleteTransaction(model.getId())) {
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        // Click to Update (Reuses AddExpenseActivity with intent extras)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddExpenseActivity.class);
            intent.putExtra("id", model.getId());
            intent.putExtra("title", model.getTitle());
            intent.putExtra("category", model.getCategory());
            intent.putExtra("amount", model.getAmount());
            intent.putExtra("date", model.getDate());
            intent.putExtra("type", model.getType());
            intent.putExtra("isEdit", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTitle, tvCategory, tvAmount;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            imgIcon = itemView.findViewById(R.id.imgIcon);
        }
    }
}

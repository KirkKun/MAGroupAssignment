package com.chenghakfan.magroupassignment;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    private ArrayList<DebtModel> debtList;
    private DatabaseHelper db;
    private Context context;

    public DebtAdapter(ArrayList<DebtModel> debtList, DatabaseHelper db) {
        this.debtList = debtList;
        this.db = db;
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        DebtModel debt = debtList.get(position);
        holder.tvTitle.setText(debt.getTitle());
        holder.tvAmount.setText(String.format("RM%.2f / RM%.2f", debt.getAmountPaid(), debt.getTotalAmount()));

        int progress = (int) ((debt.getAmountPaid() / debt.getTotalAmount()) * 100);
        holder.progressBar.setProgress(progress);

        holder.btnDelete.setOnClickListener(v -> {
            if (db.deleteDebt(debt.getId())) {
                debtList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, debtList.size());
            }
        });

        holder.btnPay.setOnClickListener(v -> showUpdateDialog(debt, position));
    }

    private void showUpdateDialog(DebtModel debt, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Payment for " + debt.getTitle());

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount paid today");
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String val = input.getText().toString();
            if (!val.isEmpty()) {
                double addedAmount = Double.parseDouble(val);
                double newTotalPaid = debt.getAmountPaid() + addedAmount;
                if (newTotalPaid > debt.getTotalAmount()) {
                    Toast.makeText(context, "Payment cannot exceed total debt!", Toast.LENGTH_SHORT).show();
                } else {
                    if (db.updateDebtProgress(debt.getId(), newTotalPaid)) {
                        debtList.set(position, new DebtModel(debt.getId(), debt.getTitle(), debt.getTotalAmount(), newTotalPaid));
                        notifyItemChanged(position);
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return debtList.size();
    }

    public static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount;
        ProgressBar progressBar;
        ImageView btnDelete;
        Button btnPay;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvDebtTitle);
            tvAmount = itemView.findViewById(R.id.tvDebtAmount);
            progressBar = itemView.findViewById(R.id.pbDebtProgress);
            btnDelete = itemView.findViewById(R.id.btnDeleteDebt);
            btnPay = itemView.findViewById(R.id.btnPayDebt);
        }
    }
}

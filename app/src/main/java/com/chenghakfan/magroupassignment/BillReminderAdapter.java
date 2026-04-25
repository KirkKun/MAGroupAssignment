package my.edu.utar.mobileappass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class BillReminderAdapter extends RecyclerView.Adapter<BillReminderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<BillReminderModel> list;
    private OnBillActionListener listener;

    public interface OnBillActionListener {
        void onEdit(BillReminderModel model);
        void onDelete(BillReminderModel model);
    }

    public BillReminderAdapter(Context context, ArrayList<BillReminderModel> list, OnBillActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BillReminderModel model = list.get(position);
        holder.tvTitle.setText(model.getTitle());
        holder.tvDetail.setText(String.format(Locale.getDefault(), "Due: %s • RM%.2f", model.getDueDate(), model.getAmount()));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(model));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(model));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDetail;
        ImageView btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBillTitle);
            tvDetail = itemView.findViewById(R.id.tvBillDetail);
            btnEdit = itemView.findViewById(R.id.btnEditBill);
            btnDelete = itemView.findViewById(R.id.btnDeleteBill);
        }
    }
}

package com.chenghakfan.magroupassignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private ArrayList<ChatMessage> chatMessages;

    public ChatAdapter(ArrayList<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.isUser()) {
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutAi.setVisibility(View.GONE);
            holder.tvUserMsg.setText(message.getMessage());
        } else {
            holder.layoutAi.setVisibility(View.VISIBLE);
            holder.layoutUser.setVisibility(View.GONE);
            holder.tvAiMsg.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutAi, layoutUser;
        TextView tvAiMsg, tvUserMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutAi = itemView.findViewById(R.id.layoutAi);
            layoutUser = itemView.findViewById(R.id.layoutUser);
            tvAiMsg = itemView.findViewById(R.id.tvAiMsg);
            tvUserMsg = itemView.findViewById(R.id.tvUserMsg);
        }
    }
}

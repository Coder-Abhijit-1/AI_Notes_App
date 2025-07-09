package com.example.android.ainotesapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_NOBI = 2;

    private ArrayList<ChatMessage> chatMessages;
    private Context context;

    public ChatAdapter(ArrayList<ChatMessage> chatMessages, Context context) {
        this.chatMessages = chatMessages;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_NOBI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_user_message, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_nobi_message, parent, false);
            return new NobiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(message.getTimestamp()));

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).userMessage.setText(message.getMessage());
            ((UserViewHolder) holder).timestamp.setText(time);
        } else {
            ((NobiViewHolder) holder).nobiMessage.setText(message.getMessage());
            ((NobiViewHolder) holder).timestamp.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userMessage, timestamp;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessage = itemView.findViewById(R.id.userMessage);
            timestamp = itemView.findViewById(R.id.userTime);
        }
    }

    static class NobiViewHolder extends RecyclerView.ViewHolder {
        TextView nobiMessage, timestamp;

        public NobiViewHolder(@NonNull View itemView) {
            super(itemView);
            nobiMessage = itemView.findViewById(R.id.nobiMessage);
            timestamp = itemView.findViewById(R.id.nobiTime);
        }
    }
}

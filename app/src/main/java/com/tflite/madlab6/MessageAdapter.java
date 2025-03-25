package com.tflite.madlab6;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<ChatMessage> messageList;
    public MessageAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage chatMessage = messageList.get(position);

        // If the message is empty, show a blank line
        if (chatMessage.getSender().isEmpty() && chatMessage.getChoice().isEmpty()) {
            holder.gameOutput.setText(""); // Blank line
        } else {
            holder.gameOutput.setText(chatMessage.getSender() + ": " + chatMessage.getChoice() + "\t\t" + " Score: " + chatMessage.getUserScore());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void clearMessages() {
        messageList.clear();
        notifyDataSetChanged();
    }
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView gameOutput;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            gameOutput = itemView.findViewById(R.id.gameOutput);
        }
    }
}

package com.example.android.ainotesapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText inputMessage;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatMessages;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        recyclerView = findViewById(R.id.recyclerViewNobi);
        inputMessage = findViewById(R.id.inputEditTextNobi);
        sendButton = findViewById(R.id.sendButtonNobi);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendButton.setOnClickListener(v -> {
            String userMessage = inputMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, true); // User message
                inputMessage.setText("");
                addMessage(getNobiReply(userMessage), false); // Nobi’s reply
            }
        });
    }

    private void addMessage(String message, boolean isUser) {
        ChatMessage chatMessage = new ChatMessage(message, isUser, new Date().getTime());
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    // 🔁 Dummy Nobi reply logic
    private String getNobiReply(String userMessage) {
        userMessage = userMessage.toLowerCase();

        if (userMessage.contains("hello") || userMessage.contains("hi") || userMessage.contains("hey")) {
            return "Hey buddy! 😊 Nobi is here for you!";
        }
        if (userMessage.contains("how are you")) {
            return "I'm feeling super smart and ready to help you with your notes!";
        }
        if (userMessage.contains("note") || userMessage.contains("save") || userMessage.contains("add")) {
            return "Just tap the + button to add a new note. Let’s keep your ideas safe!";
        }
        if (userMessage.contains("reminder") || userMessage.contains("notify")) {
            return "You can set reminders easily! I’ll help you remember everything.";
        }
        if (userMessage.contains("joke")) {
            return "Why don’t programmers like nature? Too many bugs! 😄";
        }
        if (userMessage.contains("who are you")) {
            return "I'm Nobi, your smart little notes assistant built just for you!";
        }
        if (userMessage.contains("bye") || userMessage.contains("goodbye")) {
            return "Goodbye bestie! Come back soon — Nobi will be waiting 🐾";
        }
        if (userMessage.contains("thank")) {
            return "Aww, you're welcome! Always happy to help 💖";
        }
        if (userMessage.contains("help")) {
            return "I can help you add, view, edit, and organize your notes. Just ask!";
        }
        if (userMessage.contains("time")) {
            return "It’s always the right time to take notes! 😉";
        }
        if (userMessage.contains("date")) {
            return "Today is " + new java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault()).format(new java.util.Date());
        }
        if (userMessage.contains("love")) {
            return "Love you too! You're my favorite user! ❤️";
        }

        // Default fallback
        return "Hmm... I’m still learning! Can you try saying that another way?";
    }


}

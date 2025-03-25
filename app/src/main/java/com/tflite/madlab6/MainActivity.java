package com.tflite.madlab6;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tflite.madlab6.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    DatabaseReference myRef;
    MessageAdapter adapter;
    private List<ChatMessage> messageList;
    ChatMessage chatMessage;
    int user1Score = 0;
    int user2Score = 0;
    private String user1Choice = "";
    private String user2Choice = "";
    private String turn = "User1";
    private String currentPlayer;
    private String assignedPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        binding.messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.messageRecyclerView.setAdapter(adapter);

        ImageButton rockButton = binding.rockButton;
        ImageButton paperButton = binding.paperButton;
        ImageButton scissorsButton = binding.scissorsButton;

//        myRef.removeValue();

        // Set listeners for buttons
        rockButton.setOnClickListener(v -> handleChoice("Rock"));
        paperButton.setOnClickListener(v -> handleChoice("Paper"));
        scissorsButton.setOnClickListener(v -> handleChoice("Scissors"));

        // Auto-assign player
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        assignedPlayer = prefs.getString("assignedPlayer", "");

        // Default to User1 if no role is assigned
        if (assignedPlayer.isEmpty()) {
            assignedPlayer = "User1";
            prefs.edit().putString("assignedPlayer", assignedPlayer).apply();
        }

        myRef.child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                turn = snapshot.getValue(String.class);
                updateButtonState();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error updating turn", Toast.LENGTH_SHORT).show();
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                adapter.clearMessages();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    chatMessage = childSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        adapter.addMessage(chatMessage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonState() {
        boolean changeTurn = turn != null && turn.equals(assignedPlayer);

        binding.rockButton.setEnabled(changeTurn);
        binding.paperButton.setEnabled(changeTurn);
        binding.scissorsButton.setEnabled(changeTurn);
    }

    private void updateTurnIndicator() {
        // Set turn text based on the current player's turn
        if ("User1".equals(turn)) {
            binding.turnTextView.setText(getString(R.string.player_1_s_turn));
            binding.turnTextView.setTextColor(getResources().getColor(R.color.player_1));
        } else {
            binding.turnTextView.setText(getString(R.string.player_2_s_turn));
            binding.turnTextView.setTextColor(getResources().getColor(R.color.player_2));
        }
    }

    private void handleChoice(String choice) {
        if ("User1".equals(turn)) {
            user1Choice = choice;
            turn = "User2"; // Switch turn
        } else if ("User2".equals(turn)) {
            user2Choice = choice;
            turn = "User1"; // Switch turn
        }

        // Update turn indicator after choice
        updateTurnIndicator();

        // Determine the winner of the round
        if (!user1Choice.isEmpty() && !user2Choice.isEmpty()) {
            if (user1Choice.equals(user2Choice)) {
                user1Choice = "";
                user2Choice = "";
                // It's a tie, no score update
                return;
            }
            if ((user1Choice.equals("Rock") && user2Choice.equals("Scissors")) ||
                    (user1Choice.equals("Scissors") && user2Choice.equals("Paper")) ||
                    (user1Choice.equals("Paper") && user2Choice.equals("Rock"))) {
                user1Score++;
            } else {
                user2Score++;
            }

            // Update scores
            binding.player1Score.setText(String.valueOf(user1Score));
            binding.player2Score.setText(String.valueOf(user2Score));

            // Create chat messages for the round
            ChatMessage user1Message = new ChatMessage("User1", "Played", user1Choice, user1Score, turn);
            ChatMessage user2Message = new ChatMessage("User2", "Played", user2Choice, user2Score, turn);

            myRef.push().setValue(user1Message);
            myRef.push().setValue(user2Message);

            // Check for winner
            if (user1Score >= 5 || user2Score >= 5) {
                String winner = (user1Score >= 5) ? "User 1 Wins!" : "User 2 Wins!";
                Toast.makeText(MainActivity.this, winner, Toast.LENGTH_SHORT).show();
                myRef.removeValue();
                user1Score = 0;
                user2Score = 0;
                binding.player1Score.setText("0");
                binding.player2Score.setText("0");
            }
            user1Choice = "";
            user2Choice = "";
        }
    }
}

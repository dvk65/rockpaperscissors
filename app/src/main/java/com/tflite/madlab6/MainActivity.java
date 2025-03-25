package com.tflite.madlab6;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
    ChatMessage chatMessage;
    int user1Score = 0;
    int user2Score = 0;
    private String user1Choice = "";
    private String user2Choice = "";
    private String turn = "User1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        List<ChatMessage> messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        binding.messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.messageRecyclerView.setAdapter(adapter);

        ImageButton rockButton = binding.rockButton;
        ImageButton paperButton = binding.paperButton;
        ImageButton scissorsButton = binding.scissorsButton;

        myRef.removeValue();

//        updateTurnIndicator();

        // Set listeners for buttons
        rockButton.setOnClickListener(v -> handleChoice("Rock"));
        paperButton.setOnClickListener(v -> handleChoice("Paper"));
        scissorsButton.setOnClickListener(v -> handleChoice("Scissors"));

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clearMessages();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    chatMessage = childSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        adapter.addMessage(chatMessage);
                    }
                }
                binding.messageRecyclerView.scrollToPosition(adapter.getItemCount() - 1); // Scroll to the last item
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTurnIndicator() {
        // Set turn text based on the current player's turn
        if ("User1".equals(turn)) {
            binding.turnTextView.setText(getString(R.string.player_1_s_turn));
            binding.turnTextView.setTextColor(ContextCompat.getColor(this, R.color.player_1));
            binding.player1ScoreLabel.setTextColor(ContextCompat.getColor(this, R.color.player_1));
            binding.player2ScoreLabel.setTextColor(ContextCompat.getColor(this, R.color.grey));
            binding.player1ScoreLabel.setTypeface(null, Typeface.BOLD);
            binding.player2ScoreLabel.setTypeface(null, Typeface.NORMAL);
        } else {
            binding.turnTextView.setText(getString(R.string.player_2_s_turn));
            binding.turnTextView.setTextColor(ContextCompat.getColor(this, R.color.player_2));
            binding.player1ScoreLabel.setTextColor(ContextCompat.getColor(this, R.color.grey));
            binding.player2ScoreLabel.setTextColor(ContextCompat.getColor(this, R.color.player_2));
            binding.player1ScoreLabel.setTypeface(null, Typeface.NORMAL);
            binding.player2ScoreLabel.setTypeface(null, Typeface.BOLD);
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
            ChatMessage blankMessage = new ChatMessage("", "", "", 0, ""); // Empty message

            myRef.push().setValue(user1Message);
            myRef.push().setValue(user2Message);
            myRef.push().setValue(blankMessage);

            // Check for winner
            if (user1Score >= 5 || user2Score >= 5) {
                String winner = (user1Score >= 5) ? "User 1 Wins!" : "User 2 Wins!";
                Toast.makeText(MainActivity.this, winner, Toast.LENGTH_SHORT).show();
                user1Score = 0;
                user2Score = 0;
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    myRef.removeValue();
                    binding.player1Score.setText("0");
                    binding.player2Score.setText("0");
                }, 3000); // Delay in milliseconds
            }
            user1Choice = "";
            user2Choice = "";
        }
    }
}

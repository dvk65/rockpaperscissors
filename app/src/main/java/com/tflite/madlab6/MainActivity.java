package com.tflite.madlab6;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    DatabaseReference myRef;
    MessageAdapter adapter;
    private List<ChatMessage> messageList;
    ChatMessage chatMessage;
    int user1Score = 0;
    int user2Score = 0;

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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        EditText messageInput = binding.messageInput;
        Button sendButton = binding.sendButton;
        Spinner optionSpinner1 = binding.optionSpinner1;
        Spinner optionSpinner2 = binding.optionSpinner2;

        Button clearButton = binding.clearButton;

        sendButton.setOnClickListener(v -> {
            final String[] user1choice = {optionSpinner1.getSelectedItem().toString()};
            final String[] user2choice = {optionSpinner2.getSelectedItem().toString()};

//            String user1choice = "";
//            String user2choice = "";

//            int user1Score = 0;
//            int user2Score = 0;

//            if (chatMessage.getSender().equals("User1")) {
////                user1choice = chatMessage.getChoice();
//                user1Score = chatMessage.getUserScore();
//            } else if (chatMessage.getSender().equals("User2")) {
////                user2choice = chatMessage.getChoice();
//                user2Score = chatMessage.getUserScore();
//            }

//            Log.d("SCORES", "Updated EventListener Scores - User1: " + user1Score + " | User2: " + user2Score);

            myRef.child("user1Score").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    user1Score = task.getResult().getValue(Long.class).intValue();
                } else {
                    user1Score = 0;
                }
                myRef.child("user2Score").get().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful() && task2.getResult().exists()) {
                        user2Score = task2.getResult().getValue(Long.class).intValue();
                    } else {
                        user2Score = 0;
                    }

                    if(!user1choice[0].isEmpty() && !user2choice[0].isEmpty()) {
                        if ((user1choice[0].equals("Rock") && user2choice[0].equals("Scissors")) ||
                                (user1choice[0].equals("Scissors") && user2choice[0].equals("Paper")) ||
                                (user1choice[0].equals("Paper") && user2choice[0].equals("Rock"))) {
                            user1Score++;
                        } else if (!user1choice[0].equals(user2choice[0])) {
                            user2Score++;
                            Log.d("SCORES", "Updated else if Scores - User1: " + user1Score + " | User2: " + user2Score);
                        }

//                        myRef.child("user1Score").setValue(user1Score);
//                        myRef.child("user2Score").setValue(user2Score);

                        ChatMessage user1Message = new ChatMessage("User1", "Played", user1choice[0], user1Score);
                        ChatMessage user2Message = new ChatMessage("User2", "Played", user2choice[0], user2Score);

                        myRef.push().setValue(user1Message);
                        myRef.push().setValue(user2Message);

                        if (user1Score == 5 || user2Score == 5) {
                            if (user1Score >= 5) {
                                Toast.makeText(MainActivity.this, "User 1 Wins!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "User 2 Wins!", Toast.LENGTH_SHORT).show();
                            }
//                            myRef.removeEventListener(this);
                            myRef.removeValue();
//                            break;
                        }
                        user1choice[0] = "";
                        user2choice[0] = "";
                    }


//                    if (!user1choice.isEmpty()) {
//                        chatMessage = new ChatMessage("User1", "Played", user1choice, user1Score);
//                        myRef.push().setValue(chatMessage);
////                        Log.d("SCORES", "Updated Scores - User1: " + user1Score + " | User2: " + user2Score);
//                    }
//
//                    if (!user2choice.isEmpty()) {
//                        chatMessage = new ChatMessage("User2", "Played", user2choice, user2Score);
//                        myRef.push().setValue(chatMessage);
//                    }
                });
            });
        });

        clearButton.setOnClickListener(view -> myRef.removeValue());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clearMessages();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                    chatMessage = childSnapshot.getValue(ChatMessage.class);
                    assert chatMessage != null;
                    adapter.addMessage(chatMessage);




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (vi, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vi.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
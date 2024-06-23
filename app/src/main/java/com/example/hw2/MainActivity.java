package com.example.hw2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        handleData(data);
                    }
                }
        );

        RecyclerView rv = findViewById(R.id.rv);
        rv.setHasFixedSize(false);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);
        rv.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(this, messages);  // Pass context and initial message list to adapter
        rv.setAdapter(adapter);

        ItemTouchHelper helper = new ItemTouchHelper(new SwipeToDeleteCallback(adapter));
        helper.attachToRecyclerView(rv);

        FloatingActionButton btn = findViewById(R.id.floatingActionButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AddMessageActivity.class);
                i.putExtra("messages", (ArrayList<Message>) messages);
                activityResultLauncher.launch(i);
            }
        });

        loadMessagesFromFirestore();
    }

    private void loadMessagesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages").orderBy("ID")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        messages.clear();
                        for (QueryDocumentSnapshot document : value) {
                            String avatar = document.getString("Avatar");
                            String name = document.getString("Name");
                            String text = document.getString("Text");
                            String id = document.getString("ID");
                            messages.add(new Message(avatar, name, text, id));
                        }
                        adapter.setMessages(messages);  // Update the adapter with the new message list
                        Log.d("Firestore", "Fetched " + messages.size() + " messages.");
                    }
                });
    }

    private void handleData(Intent data) {
        if (data != null && data.hasExtra("message")) {
            Message newMessage = (Message) data.getSerializableExtra("message");
            //messages.add(newMessage);
            adapter.notifyItemInserted(messages.size() - 1);
            addMessageToFirestore(newMessage);
        }
    }


    private void addMessageToFirestore(Message message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages").document(message.ID).set(message.getAsMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Message added successfully.");
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding message: ", e));
    }
}

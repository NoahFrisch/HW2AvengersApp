package com.example.hw2;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class MessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Check if intent and extras are not null
        if (getIntent() != null && getIntent().getExtras() != null) {
            Message message = (Message) getIntent().getSerializableExtra("message");
            if (message != null) {
                // Use the message object
                TextView nameTextView = findViewById(R.id.name);
                TextView textTextView = findViewById(R.id.text);
                ImageView avatarImageView = findViewById(R.id.avatar);

                // Set the text of TextViews and ImageView based on the message details
                nameTextView.setText(message.Name);
                textTextView.setText(message.Text);

                // Use Glide to load the avatar image
                Glide.with(this).load(message.Avatar).into(avatarImageView);
            } else {
                Log.e("MessageActivity", "No message found in intent extras");
            }
        } else {
            Log.e("MessageActivity", "Intent or Intent Extras are null");
        }
    }
}
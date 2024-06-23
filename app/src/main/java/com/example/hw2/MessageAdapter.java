package com.example.hw2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Message> Messages;
    private Context context;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.Messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = Messages.get(position);

        // Load the image into the avatar ImageView using Glide
        Glide.with(holder.Avatar.getContext()).load(message.Avatar).into(holder.Avatar);

        // Set the text of the Name and Text TextViews
        holder.Name.setText(message.Name);
        holder.Text.setText(message.Text);

        // Set up the click listener on the CardView
        holder.Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                intent.putExtra("message", message);

                // Set up a transition animation. 'cardTransition' is the transition name specified in your XML.
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        (Activity) v.getContext(), // Use the current activity context
                        holder.Card, // The view to transition from
                        "cardTransition" // The name of the shared element transition
                );

                // Start the activity with the options bundle for the animation
                v.getContext().startActivity(intent, options.toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return Messages.size();
    }

    public void DeleteMessage(int pos) {
        Message m = Messages.get(pos);
        db.collection("messages").document(m.ID).delete()
                .addOnSuccessListener(aVoid -> {

                })
                .addOnFailureListener(e -> {
                });
    }

    public void setMessages(List<Message> messages) {
        this.Messages = messages;
        notifyDataSetChanged();
    }

    private void fetchMessages() {
        db.collection("messages")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Messages.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message c = new Message(document.get("Avatar").toString(), document.get("Name").toString(), document.get("Text").toString(), document.get("ID").toString());
                            Messages.add(c);
                        }
                        notifyDataSetChanged();
                    }
                });

        db.collection("messages").addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                Messages.clear();
                for (QueryDocumentSnapshot document : value) {
                    Message c = new Message(document.get("Avatar").toString(), document.get("Name").toString(), document.get("Text").toString(), document.get("ID").toString());
                    Messages.add(c);
                }
                notifyDataSetChanged();
            }
        });
    }
}

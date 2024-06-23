package com.example.hw2;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;

public class AddMessageActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView addPhoto;
    int REQUEST_PERMISSIONS_CODE = 1;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri CurrentImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_message);

        // Retrieve the message list from the intent
        List<Message> messages = (List<Message>) getIntent().getSerializableExtra("messages");

        addPhoto = findViewById(R.id.addPhoto);

        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                addPhoto.setImageURI(CurrentImage);
            }
        });
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(AddMessageActivity.this, new String[] {
                            Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_MEDIA_LOCATION
                    }, REQUEST_PERMISSIONS_CODE);
                } else {
                    captureImage();
                }
            }
        });
        Button btn = findViewById(R.id.submit_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = findViewById(R.id.name);
                EditText text = findViewById(R.id.text);
                if (CurrentImage == null) {
                    Toast.makeText(AddMessageActivity.this, "No image captured", Toast.LENGTH_SHORT).show();
                    return;
                }
                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference fileRef = storageRef.child("images/" + CurrentImage.getLastPathSegment());
                fileRef.putFile(CurrentImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Message newMessage = new Message(uri.toString(), name.getText().toString(), text.getText().toString());
                                messages.add(newMessage);
                                db.collection("messages").document(newMessage.ID).set(newMessage.getAsMap())
                                        .addOnSuccessListener(aVoid -> {
                                            Intent resultIntent = new Intent();
                                            resultIntent.putExtra("message", newMessage);
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        });
                            }
                        });
                    }
                });
            }
        });
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void captureImage() {
        Uri imageUri = createImageUri();
        if (imageUri != null) {
            CurrentImage = imageUri;
            takePictureLauncher.launch(imageUri);
        }
    }
}

package com.example.android.ainotesapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PreviewNoteActivity extends AppCompatActivity {

    private TextView noteTitle, noteContent, noteTime;
    private Button editButton, shareButton, deleteButton;

    private Note currentNote;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_note);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Initialize views
        noteTitle = findViewById(R.id.previewNoteTitle);
        noteContent = findViewById(R.id.previewNoteContent);
        noteTime = findViewById(R.id.previewNoteTime);
        editButton = findViewById(R.id.editNoteBtn);
        shareButton = findViewById(R.id.shareNoteBtn);
        deleteButton = findViewById(R.id.deleteNoteBtn);


        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Get note content form internet
        databaseRef = FirebaseDatabase.getInstance().getReference("notes");

        Note currentNote = (Note) getIntent().getSerializableExtra("note");

        if (currentNote != null) {
            // Populate views
            noteTitle.setText(currentNote.getNoteInput());
            noteContent.setText(currentNote.getContent());

            String formattedDate = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    .format(new Date(currentNote.getTimeStamp()));
            noteTime.setText(formattedDate);
        } else {
            Toast.makeText(this, "Note data not found", Toast.LENGTH_SHORT).show();
            finish();
        }



        // Edit button action
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreviewNoteActivity.this, EditNoteActivity.class);
            intent.putExtra("noteId",  currentNote.getId());
            startActivity(intent);
        });

        // Share button action
        shareButton.setOnClickListener(v -> {
            String shareText = "Title: " + currentNote.getNoteInput() + "\n\n" + currentNote.getContent();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share note via..."));
        });

        // Delete button with confirmation
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        databaseRef.child(currentNote.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                                    finish(); // Go back to AllNotesActivity
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Deletion failed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
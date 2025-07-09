package com.example.android.ainotesapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AllNotesActivity extends AppCompatActivity {

    private RecyclerView notesRecyclerView;
    private NotesAdapter adapter;
    private List<Note> noteList;        // List shown in RecyclerView
    private List<Note> fullNoteList;    // Backup of all notes for search
    private DatabaseReference databaseRef;
    private ImageView logoutButton;
    private SearchView searchView;
    private ProgressBar progressBar;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);

        // Set light status bar text on light background (for Android M+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ShapeableImageView nobiFab = findViewById(R.id.nobiFab);
        nobiFab.setOnClickListener(v -> {
            Intent intent = new Intent(AllNotesActivity.this, ChatBotActivity.class);
            startActivity(intent);
        });


        textToSpeech = new TextToSpeech(this , status -> {
            if( status != TextToSpeech.ERROR){
                textToSpeech.setLanguage(Locale.getDefault());
            }
        });




        // ✅ Initialize UI elements
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        logoutButton = findViewById(R.id.logoutBtn);
        searchView = findViewById(R.id.searchView);
        FloatingActionButton addNoteFab = findViewById(R.id.addNoteFab);
        progressBar = findViewById(R.id.progressBar);

        // Make whole search view clickable and focusable
        searchView.setIconifiedByDefault(false); // keeps it expanded
        searchView.clearFocus(); // optional: prevent keyboard from auto-popping

// Make left icon (search icon) also focus the text field
        searchView.setOnClickListener(v -> {
            searchView.setIconified(false); // ensures text field is expanded
            searchView.requestFocusFromTouch(); // open keyboard
        });


        // ✅ Initialize Firebase DB reference
        databaseRef = FirebaseDatabase.getInstance().getReference("notes");

        // ✅ Initialize note lists
        noteList = new ArrayList<>();
        fullNoteList = new ArrayList<>();

        // ✅ Setup RecyclerView
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotesAdapter(this, noteList, databaseRef, textToSpeech, note -> {
            // On note item click, open EditNoteActivity with note details
            Intent intent = new Intent(AllNotesActivity.this, PreviewNoteActivity.class);
            intent.putExtra("noteId", getIntent().getStringExtra("noteId"));
            intent.putExtra("noteTitle", getIntent().getStringExtra("noteTitle"));
            intent.putExtra("noteContent", getIntent().getStringExtra("noteContent"));
            startActivity(intent);
        });
        notesRecyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Note swipedNote = noteList.get(position);

                // Toggle pinned state
                boolean newPinState = !swipedNote.isPinned();
                swipedNote.setPinned(newPinState);

                // Update in Firebase
                databaseRef.child(swipedNote.getId()).child("pinned").setValue(newPinState)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(AllNotesActivity.this,
                                    newPinState ? "Note Pinned" : "Note Unpinned",
                                    Toast.LENGTH_SHORT).show();

                            // Re-sort notes
                            Collections.sort(noteList, (n1, n2) -> Boolean.compare(n2.isPinned(), n1.isPinned()));
                            adapter.notifyDataSetChanged();
                        });
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(notesRecyclerView);

        // ✅ Fetch notes from Firebase
        fetchNotesFromFirebase();

        // ✅ Add Note FAB click
        addNoteFab.setOnClickListener(v -> {
            Intent intent = new Intent(AllNotesActivity.this, AddNewNoteActivity.class);
            startActivity(intent);
        });

        // ✅ Logout button with confirmation
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(AllNotesActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(AllNotesActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(AllNotesActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // ✅ Setup SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // 🔄 Fetch notes from Firebase and update RecyclerView
    private void fetchNotesFromFirebase() {
        progressBar.setVisibility(View.VISIBLE); // Show progress while loading
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot noteSnap : snapshot.getChildren()) {
                    Note note = noteSnap.getValue(Note.class);
                    if (note != null) {
                        noteList.add(note);
                    }
                }

                // Always keep backup of full notes for searching
                fullNoteList.clear();
                fullNoteList.addAll(noteList);

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllNotesActivity.this, "Failed to load notes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔍 Filter notes when user searches by title or content
    private void filterNotes(String query) {
        List<Note> filtered = new ArrayList<>();
        for (Note note : fullNoteList) {
            if (note.getNoteInput().toLowerCase().contains(query.toLowerCase()) ||
                    note.getContent().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(note);
            }
        }
        adapter.filterList(filtered);
    }
}

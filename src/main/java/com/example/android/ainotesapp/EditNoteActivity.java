package com.example.android.ainotesapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText;
    private Button updateButton, setReminderBtn;
    private DatabaseReference databaseRef;
    private String noteId;
    private Calendar reminderCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        titleEditText = findViewById(R.id.updatedEditTitle);
        contentEditText = findViewById(R.id.updatedEditContent);
        updateButton = findViewById(R.id.updateButton);
        setReminderBtn = findViewById(R.id.setReminderBtnEditActivity); // Add this button in your layout XML

        reminderCalendar = Calendar.getInstance();

        // Get noteId from intent
        noteId = getIntent().getStringExtra("noteId");

        if (noteId == null || noteId.isEmpty()) {
            Toast.makeText(this, "Note ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase DB reference
        databaseRef = FirebaseDatabase.getInstance().getReference("notes");

        // Fetch note data from database
        fetchNoteData();

        // Update button listener
        updateButton.setOnClickListener(v -> updateNote());

        // Reminder button listener
        setReminderBtn.setOnClickListener(v -> showTimePicker());
    }

    private void fetchNoteData() {
        databaseRef.child(noteId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String title = snapshot.child("noteInput").getValue(String.class);
                String content = snapshot.child("content").getValue(String.class);

                titleEditText.setText(title != null ? title : "");
                contentEditText.setText(content != null ? content : "");
            } else {
                Toast.makeText(this, "Note not found!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            reminderCalendar.set(Calendar.MINUTE, minute);
            reminderCalendar.set(Calendar.SECOND, 0);
            reminderCalendar.set(Calendar.MILLISECOND, 0);

            Toast.makeText(this, "Reminder set for " +
                            new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(reminderCalendar.getTime()),
                    Toast.LENGTH_SHORT).show();
        }, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    private void updateNote() {
        String updatedTitle = titleEditText.getText().toString().trim();
        String updatedContent = contentEditText.getText().toString().trim();
        long updatedTime = System.currentTimeMillis();
        long reminderTime = reminderCalendar.getTimeInMillis();

        if (updatedTitle.isEmpty() || updatedContent.isEmpty()) {
            Toast.makeText(this, "Title or content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Note updatedNote = new Note(noteId, updatedTitle, updatedContent, updatedTime, false);
        updatedNote.setReminderTime(reminderTime); // Set reminder time in note

        databaseRef.child(noteId).setValue(updatedNote)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    scheduleReminder(updatedNote); // 🔔 Schedule reminder
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleReminder(Note note) {
        if (note.getReminderTime() <= System.currentTimeMillis()) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("noteTitle", note.getNoteInput());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, note.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, note.getReminderTime(), pendingIntent);
    }
}

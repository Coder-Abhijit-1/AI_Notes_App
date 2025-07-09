package com.example.android.ainotesapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddNewNoteActivity extends AppCompatActivity {

    private static final int REQUEST_TITLE_SPEECH = 1;
    private static final int REQUEST_CONTENT_SPEECH = 2;

    private EditText titleEditText, contentEditText;
    private Button titleMicButton, contentMicButton, saveNoteBtn;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        titleMicButton = findViewById(R.id.titleMicButton);
        contentMicButton = findViewById(R.id.contentMicButton);
        saveNoteBtn = findViewById(R.id.saveNoteBtn);


        databaseRef = FirebaseDatabase.getInstance().getReference("notes");

        titleMicButton.setOnClickListener(v -> startVoiceInput(REQUEST_TITLE_SPEECH));
        contentMicButton.setOnClickListener(v -> startVoiceInput(REQUEST_CONTENT_SPEECH));



        // Step 3: Time Picker to set reminder time
        Calendar reminderCalendar = Calendar.getInstance();
        Button setReminderBtn = findViewById(R.id.setReminderBtn);

        setReminderBtn.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE,minute);
                reminderCalendar.set(Calendar.SECOND, 0);
                reminderCalendar.set(Calendar.MILLISECOND, 0);

                Toast.makeText(this, "Reminder set for " +
                        new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(reminderCalendar.getTime()), Toast.LENGTH_SHORT).show();

            }, reminderCalendar.get(Calendar.HOUR_OF_DAY), reminderCalendar.get(Calendar.MINUTE), false);
            timePickerDialog.show();
            });
        saveNoteBtn.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String content = contentEditText.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter both title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            String noteId = databaseRef.push().getKey();
            long timeStamp = System.currentTimeMillis();

            if (noteId != null) {
                // ✅ Get reminder time from the calendar
                long reminderTimeInMillis = reminderCalendar.getTimeInMillis();

                // ✅ Create Note object with reminder time
                Note note = new Note();
                note.setId(noteId);
                note.setNoteInput(title);
                note.setContent(content);
                note.setTimeStamp(timeStamp);
                note.setPinned(false);
                note.setReminderTime(reminderTimeInMillis); // Set reminder time

                // ✅ Save to Firebase
                databaseRef.child(noteId).setValue(note)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();

                            // ✅ Schedule reminder notification
                            scheduleReminder(note);

                            // ✅ Reset fields and go back
                            titleEditText.setText("");
                            contentEditText.setText("");
                            startActivity(new Intent(AddNewNoteActivity.this, AllNotesActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }


    // Step 4: Schedule reminder using AlarmManager
    private void scheduleReminder(Note note) {
        if (note.getReminderTime() <= System.currentTimeMillis()) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("noteTitle", note.getNoteInput());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, note.getId().hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, note.getReminderTime(), pendingIntent);
    }


    private void startVoiceInput(int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);

                if (requestCode == REQUEST_TITLE_SPEECH) {
                    // Replace title completely
                    titleEditText.setText(spokenText);

                } else if (requestCode == REQUEST_CONTENT_SPEECH) {
                    // Append to existing content
                    String existing = contentEditText.getText().toString();
                    if (!existing.isEmpty()) {
                        contentEditText.setText(existing + " " + spokenText);
                    } else {
                        contentEditText.setText(spokenText);
                    }
                }
            }
        }
    }




}

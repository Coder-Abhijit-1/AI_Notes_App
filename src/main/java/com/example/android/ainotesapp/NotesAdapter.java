package com.example.android.ainotesapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> noteList;
    private OnNoteClickListener listener;
    private Context context;
    private DatabaseReference databaseRef;
    private TextToSpeech tts;

    public interface OnNoteClickListener{
        void onNoteClick(Note note);
    }
    public NotesAdapter(Context context, List<Note> noteList, DatabaseReference databaseRef, TextToSpeech tts , OnNoteClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.databaseRef = databaseRef;
        this.tts = tts;
        this.listener = listener;
    }


    public void filterList(List<Note> filterList){
        this.noteList = filterList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));

        holder.titleText.setText(note.getNoteInput());

        // Get first line of note content
        String[] lines = note.getContent().split("\n");
        String firstLine = lines.length > 0 ? lines[0] : "";
        holder.contentPreview.setText(firstLine);

        // Format and display timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(note.getTimeStamp()));
        holder.timeText.setText(formattedDate);
        holder.deleteNoteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        String noteId = note.getId();
                        databaseRef.child(noteId).removeValue()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
                                    noteList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, noteList.size());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Set pin icon based on state
        if (note.isPinned()) {
            holder.pinButton.setImageResource(R.drawable.baseline_push_pin_24);
            holder.pinButton.setColorFilter(ContextCompat.getColor(context, R.color.pinActiveColor));
        } else {
            holder.pinButton.setImageResource(R.drawable.baseline_push_pin_24);
            holder.pinButton.setColorFilter(ContextCompat.getColor(context, R.color.pinInactiveColor));
        }


// Toggle pin state
        holder.pinButton.setOnClickListener(v -> {
            boolean newPinState = !note.isPinned();
            note.setPinned(newPinState);

            databaseRef.child(note.getId()).child("pinned").setValue(newPinState)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, newPinState ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                    });

            // Optional: Re-sort list after pin change
            Collections.sort(noteList, (n1, n2) -> Boolean.compare(n2.isPinned(), n1.isPinned()));
            notifyDataSetChanged();
        });
        holder.itemView.setTranslationX(0); // reset position after swipe

        holder.speakNoteBtn.setOnClickListener(v -> {
            String fullText = "Title: " + note.getNoteInput() + ". Note content: " + note.getContent();
            tts.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, null);
        });


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PreviewNoteActivity.class);
            intent.putExtra("note", note); // Passing Note object
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, contentPreview, timeText;
        ImageButton deleteNoteBtn, pinButton, speakNoteBtn;


        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            speakNoteBtn = itemView.findViewById(R.id.speakNoteBtn);
            titleText = itemView.findViewById(R.id.noteTitle);
            contentPreview = itemView.findViewById(R.id.noteContent);
            timeText = itemView.findViewById(R.id.noteTime);
            timeText = itemView.findViewById(R.id.noteTime);
            deleteNoteBtn = itemView.findViewById(R.id.deleteNoteBtn); // 👈 Initialize here

            pinButton = itemView.findViewById(R.id.pinButton);


        }
    }
}

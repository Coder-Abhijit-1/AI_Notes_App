package com.example.android.ainotesapp;

import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    private String noteInput;  // This will act as Title
    private String content;    // Full content of the note
    private long timeStamp;
    public boolean pinned;
    private long reminderTime;

    public boolean isPinned(){
        return pinned;
    }

    public void setPinned(boolean pinned){
        this.pinned = pinned;
    }

    // Empty constructor (required for Firebase)
    public Note() {
    }

    // Constructor with all fields
    public Note(String id, String noteInput, String content, long timeStamp, boolean pinned) {
        this.id = id;
        this.noteInput = noteInput;
        this.content = content;
        this.timeStamp = timeStamp;
        this.pinned = pinned;
    }


    // Getters and Setters

    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoteInput() {
        return noteInput;
    }

    public void setNoteInput(String noteInput) {
        this.noteInput = noteInput;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}

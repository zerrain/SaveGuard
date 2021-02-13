package com.zkthesis.saveguard;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

    private String noteTitle;
    private String noteText;
    private String dateAdded;
    private String timeAdded;
    private String shiftTaken;

    public Note() {
    }

    public Note(String noteTitle, String noteText, String dateAdded, String timeAdded) {
        this.noteTitle = noteTitle;
        this.noteText = noteText;
        this.dateAdded = dateAdded;
        this.timeAdded = timeAdded;
    }

    protected Note(Parcel in) {
        noteTitle = in.readString();
        noteText = in.readString();
        dateAdded = in.readString();
        timeAdded = in.readString();
        shiftTaken = in.readString();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    @Override
    public String toString() {
        return timeAdded + "," + dateAdded + "," + noteTitle + "," + noteText;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(noteTitle);
        dest.writeString(noteText);
        dest.writeString(dateAdded);
        dest.writeString(timeAdded);
        dest.writeString(shiftTaken);
    }
}

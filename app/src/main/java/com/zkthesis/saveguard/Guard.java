package com.zkthesis.saveguard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Guard implements Parcelable {
    private String fullName;
    private String currentRole;
    private ArrayList<Note> notes;

    public Guard() {
    }

    protected Guard(Parcel in) {
        fullName = in.readString();
        currentRole = in.readString();
        notes = in.readArrayList(Note.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(currentRole);
        dest.writeList(notes);
    }

    public static final Creator<Guard> CREATOR = new Creator<Guard>() {
        @Override
        public Guard createFromParcel(Parcel in) {
            return new Guard(in);
        }

        @Override
        public Guard[] newArray(int size) {
            return new Guard[size];
        }
    };

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Guard{" +
                "fullName='" + fullName + '\'' +
                ", currentRole='" + currentRole + '\'' +
                ", notes=" + notes +
                '}';
    }
}

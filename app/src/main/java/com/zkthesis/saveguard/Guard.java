package com.zkthesis.saveguard;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Guard implements Parcelable {
    private String fullName;
    private String currentRole;
    private String uID;
    private String token;
    private ArrayList<Note> notes;

    public Guard() {
    }

    protected Guard(Parcel in) {
        fullName = in.readString();
        currentRole = in.readString();
        uID = in.readString();
        token = in.readString();
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
        dest.writeString(uID);
        dest.writeString(token);
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

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

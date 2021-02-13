package com.zkthesis.saveguard.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zkthesis.saveguard.Note;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

    private ArrayList<Note> notes;
    private Context context;

    public static class NotesViewHolder extends RecyclerView.ViewHolder {

        public TextView noteTitle;
        public TextView noteText;
        public TextView dateAdded;
        public TextView timeAdded;


        public NotesViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitleRVTV);
            noteText = itemView.findViewById(R.id.noteTextRVTV);
            dateAdded = itemView.findViewById(R.id.notedateAddedRVTV);
            timeAdded = itemView.findViewById(R.id.notetimeAddedRVTV);
        }
    }

    public NotesAdapter(ArrayList<Note> notes, Context context) {
        this.notes = notes;
        this.context = context;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_notes, parent, false);

        return new NotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NotesViewHolder holder, int position) {
        int notePosition = holder.getLayoutPosition();
        holder.noteTitle.setText(notes.get(notePosition).getNoteTitle());
        holder.noteText.setText(notes.get(notePosition).getNoteText());
        holder.timeAdded.setText(notes.get(notePosition).getTimeAdded());
        holder.dateAdded.setText(notes.get(notePosition).getDateAdded());

        holder.itemView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(holder.noteTitle.getText().toString());
            builder.setMessage(holder.noteText.getText().toString());
            builder.setCancelable(true);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}

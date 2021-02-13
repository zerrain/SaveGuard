package com.zkthesis.saveguard.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zkthesis.saveguard.Activities.CreateNoteActivity;
import com.zkthesis.saveguard.Activities.MainActivity;
import com.zkthesis.saveguard.Adapters.NotesAdapter;
import com.zkthesis.saveguard.Note;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NotesFragment extends Fragment {

    @BindView(R.id.noNotesTV)
    TextView noNotesTV;
    @BindView(R.id.notesRecyclerView)
    RecyclerView notesRecyclerView;

    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    protected ArrayList<Note> notes;

    public NotesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notes, container, false);
        ButterKnife.bind(this, v);

        MaterialToolbar toolbar =  getActivity().findViewById(R.id.mainToolbar);
        toolbar.getMenu().clear();

        notes = new ArrayList<>();

        notesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        notesRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(notesRecyclerView.getContext(),
                getResources().getConfiguration().orientation);
        notesRecyclerView.addItemDecoration(dividerItemDecoration);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users").child(user.getUid()).child("Profile");

        reference.child("Notes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null || snapshot.getChildrenCount() == 0) {
                    noNotesTV.setVisibility(View.VISIBLE);
                    notesRecyclerView.setVisibility(View.GONE);
                }
                else {
                    for (DataSnapshot noteSnapshot : snapshot.getChildren()) {
                        Note note = new Note();
                        note.setNoteTitle((String) noteSnapshot.child("noteTitle").getValue());
                        note.setNoteText((String) noteSnapshot.child("noteText").getValue());
                        note.setDateAdded((String) noteSnapshot.child("dateAdded").getValue());
                        note.setTimeAdded((String) noteSnapshot.child("timeAdded").getValue());
                        notes.add(note);
                    }
                    noNotesTV.setVisibility(View.GONE);
                    notesRecyclerView.setVisibility(View.VISIBLE);
                    Collections.reverse(notes);
                    RecyclerView.Adapter notesAdapater = new NotesAdapter(notes, getContext());
                    notesRecyclerView.setAdapter(notesAdapater);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }

    @OnClick(R.id.addNoteFAB)
    public void addNote() {
        startActivity(new Intent(getActivity(), CreateNoteActivity.class).putExtra("currentShift", ((MainActivity) getActivity()).getCurrentShift()));
        getActivity().finish();
    }
}
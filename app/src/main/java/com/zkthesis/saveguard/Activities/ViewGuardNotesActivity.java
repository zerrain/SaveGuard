package com.zkthesis.saveguard.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.zkthesis.saveguard.Adapters.NotesAdapter;
import com.zkthesis.saveguard.Guard;
import com.zkthesis.saveguard.Note;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewGuardNotesActivity extends AppCompatActivity {

    @BindView(R.id.guardNotesToolbar)
    MaterialToolbar guardNotesToolbar;
    @BindView(R.id.noGuardNotesTV)
    TextView noGuardNotesTV;
    @BindView(R.id.guardNotesRecyclerView)
    RecyclerView guardNotesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_guard_notes);
        ButterKnife.bind(this);

        Guard guard = getIntent().getExtras().getParcelable("guard");
        ArrayList<Note> guardNotes = guard.getNotes();
        Collections.reverse(guardNotes);

        guardNotesToolbar.setTitle(guard.getFullName() + "'s Notes");

        if (guardNotes.size() == 0) {
            noGuardNotesTV.setVisibility(View.VISIBLE);
            guardNotesRecyclerView.setVisibility(View.GONE);
        }
        else {
            noGuardNotesTV.setVisibility(View.GONE);
            guardNotesRecyclerView.setVisibility(View.VISIBLE);
        }

        guardNotesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        guardNotesRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(guardNotesRecyclerView.getContext(),
                getResources().getConfiguration().orientation);
        guardNotesRecyclerView.addItemDecoration(dividerItemDecoration);

        RecyclerView.Adapter guardNotesAdapter = new NotesAdapter(guardNotes, this);
        guardNotesRecyclerView.setAdapter(guardNotesAdapter);
    }
}
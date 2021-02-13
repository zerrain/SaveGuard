package com.zkthesis.saveguard.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zkthesis.saveguard.Activities.CreateShiftActivity;
import com.zkthesis.saveguard.Activities.MainActivity;
import com.zkthesis.saveguard.Adapters.GuardsOnShiftAdapter;
import com.zkthesis.saveguard.Guard;
import com.zkthesis.saveguard.Note;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShiftsFragment extends Fragment {

    @BindView(R.id.noCurrentShiftTV)
    TextView noCurrentShiftTV;
    @BindView(R.id.currentShiftLocationTV)
    TextView currentShiftLocationTV;
    @BindView(R.id.currentShiftStartTV)
    TextView currentShiftStartTV;
    @BindView(R.id.currentShiftFinishTV)
    TextView currentShiftFinishTV;
    @BindView(R.id.currentShiftDateTV)
    TextView currentShiftDateTV;
    @BindView(R.id.currentShiftLinkCodeTV)
    TextView currentShiftLinkCodeTV;
    @BindView(R.id.currentShiftLL)
    LinearLayout currentShiftLL;
    @BindView(R.id.guardsOnShiftRecyclerView)
    RecyclerView guardsOnShiftRecyclerView;
    @BindView(R.id.guardsOnShiftSwipeRefreshLayout)
    SwipeRefreshLayout guardsOnShiftSwipeRefreshLayout;

    private FirebaseUser user;
    protected FirebaseDatabase database;
    protected DatabaseReference userReference;
    private DatabaseReference shiftReference;
    protected ArrayList<String> guardsOnShift;
    protected ArrayList<Guard> guards;
    protected ArrayList<Guard> guardsSupervisors;
    protected ArrayList<String> guardTokens;
    protected String token;
    protected int guardCount = 0;

    public ShiftsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shifts, container, false);
        ButterKnife.bind(this, v);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("Users").child(user.getUid()).child("Profile");

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                return;
            token = task.getResult();
            userReference.child("token").setValue(token);
        });

        guardsOnShiftRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        guardsOnShiftRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(guardsOnShiftRecyclerView.getContext(),
                getResources().getConfiguration().orientation);
        guardsOnShiftRecyclerView.addItemDecoration(dividerItemDecoration);

        final String[] currentShiftUID = new String[1];
        guardsOnShift = new ArrayList<>();
        guardTokens = new ArrayList<>();
        guards = new ArrayList<>();
        guardsSupervisors = new ArrayList<>();
        userReference.child("currentShift").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null || snapshot.getValue().toString().equals("noshift"))
                    currentShiftUID[0] = "noshift";
                else
                    currentShiftUID[0] = snapshot.getValue().toString().trim();
                if (currentShiftUID[0].equals("noshift")) {
                    noCurrentShiftTV.setVisibility(View.VISIBLE);
                    currentShiftLL.setVisibility(View.GONE);
                } else {
                    shiftReference = database.getReference("Shifts").child(currentShiftUID[0]);
                    shiftReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            currentShiftLocationTV.setText(snapshot.child("shiftLocation").getValue().toString());
                            currentShiftStartTV.setText(snapshot.child("shiftStartTime").getValue().toString());
                            currentShiftFinishTV.setText(snapshot.child("shiftFinishTime").getValue().toString());
                            currentShiftDateTV.setText(snapshot.child("shiftDate").getValue().toString());
                            currentShiftLinkCodeTV.setText(currentShiftUID[0].substring(currentShiftUID[0].length() - 5));

                            guardsOnShift.clear();

                            guardsOnShift.addAll((ArrayList<String>) snapshot.child("guards").getValue());

                            guardCount = 0;

                            for (String guard : guardsOnShift) {
                                guardCount++;
                                database.getReference("Users").child(guard).child("Profile")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() != null) {
                                                    guardTokens.add(snapshot.child("token").getValue().toString());
                                                    Guard guardToAdd = new Guard();
                                                    guardToAdd.setFullName(snapshot.child("fullName").getValue().toString());
                                                    guardToAdd.setCurrentRole(snapshot.child("currentRole").getValue().toString());

                                                    ArrayList<Note> guardNotes = new ArrayList<>();
                                                    for (DataSnapshot dataSnapshot : snapshot.child("Notes").getChildren())
                                                        if (dataSnapshot.child("shiftTaken").getValue() != null)
                                                            if (dataSnapshot.child("shiftTaken").getValue().toString().equals(currentShiftUID[0])) {
                                                                Note note = new Note();
                                                                note.setNoteTitle(dataSnapshot.child("noteTitle").getValue().toString());
                                                                note.setNoteText(dataSnapshot.child("noteText").getValue().toString());
                                                                note.setDateAdded(dataSnapshot.child("dateAdded").getValue().toString());
                                                                note.setTimeAdded(dataSnapshot.child("timeAdded").getValue().toString());
                                                                guardNotes.add(note);
                                                            }
                                                    guardToAdd.setNotes(guardNotes);
                                                    if (guardToAdd.getCurrentRole().equals("Supervisor"))
                                                        guardsSupervisors.add(guardToAdd);
                                                    if (guardToAdd.getCurrentRole().equals("Guard"))
                                                        guards.add(guardToAdd);
                                                }
                                                if (guardCount == guardsOnShift.size()) {
                                                    guards.addAll(guardsSupervisors);
                                                    Collections.reverse(guards);
                                                    RecyclerView.Adapter guardsAdapter = new GuardsOnShiftAdapter(guards, getContext());
                                                    guardsOnShiftRecyclerView.setAdapter(guardsAdapter);
                                                    guardsOnShiftSwipeRefreshLayout.setOnRefreshListener(() -> {
                                                        ((MainActivity) getActivity()).switchToShiftsFragment();
                                                        guardsOnShiftSwipeRefreshLayout.setRefreshing(false);
                                                    });
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    noCurrentShiftTV.setVisibility(View.GONE);
                    currentShiftLL.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        MaterialToolbar toolbar = getActivity().findViewById(R.id.mainToolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_shifts);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.createShift) {
                if (currentShiftUID[0].equals("noshift")) {
                    startActivity(new Intent(getActivity(), CreateShiftActivity.class));
                    getActivity().finish();
                } else
                    Toast.makeText(getActivity(), "Unlink from current shift first", Toast.LENGTH_SHORT).show();
            } else if (item.getItemId() == R.id.joinShift)
                if (noCurrentShiftTV.getVisibility() == View.VISIBLE)
                    joinShift();
                else
                    Toast.makeText(getActivity(), "Unlink from current shift first", Toast.LENGTH_SHORT).show();
            return true;
        });

        return v;
    }

    private void joinShift() {
        View linkShiftDV = getLayoutInflater().inflate(R.layout.dialog_link_shift, null);
        EditText linkCodeET = linkShiftDV.findViewById(R.id.linkCodeET);
        RadioGroup guardTypeRG = linkShiftDV.findViewById(R.id.guardTypeRG);

        new AlertDialog.Builder(getActivity())
                .setTitle("Enter Shift Link Code")
                .setView(linkShiftDV)
                .setPositiveButton("OK", (dialog, which) -> {
                    if (linkCodeET.getText().toString().length() != 5)
                        Toast.makeText(getActivity(), "Please enter a valid shift link code", Toast.LENGTH_SHORT).show();
                    else if (guardTypeRG.getCheckedRadioButtonId() == -1)
                        Toast.makeText(getActivity(), "Please select your role for this shift", Toast.LENGTH_SHORT).show();
                    else {
                        RadioButton selectedRole = linkShiftDV.findViewById(guardTypeRG.getCheckedRadioButtonId());
                        linkShift(linkCodeET.getText().toString().trim(), selectedRole.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.cancel())
                .show();
    }

    private void linkShift(String inputShiftCode, String selectedRole) {
        database.getReference("Shifts").child("allShifts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null)
                    Toast.makeText(getActivity(), "No shifts available", Toast.LENGTH_SHORT).show();
                else {
                    ArrayList<String> allShifts = new ArrayList<>((ArrayList<String>) snapshot.getValue());
                    boolean invalidCode = true;
                    for (String shiftUID : allShifts)
                        if (shiftUID.substring(shiftUID.length() - 5).equals(inputShiftCode)) {
                            userReference.child("currentShift").setValue(shiftUID);
                            userReference.child("currentRole").setValue(selectedRole);
                            database.getReference("Shifts").child(shiftUID).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.child("guards").getValue() == null)
                                                database.getReference("Shifts").child(shiftUID).child("guards").setValue(new ArrayList<>(Arrays.asList(FirebaseAuth.getInstance().getCurrentUser().getUid())));
                                            else {
                                                guardsOnShift = (ArrayList<String>) snapshot.child("guards").getValue();
                                                guardsOnShift.add(user.getUid());
                                                database.getReference("Shifts").child(shiftUID).child("guards").setValue(guardsOnShift);
                                            }
                                            if (snapshot.child("tokens").getValue() == null)
                                                database.getReference("Shifts").child(shiftUID).child("tokens").setValue(new ArrayList<>(Arrays.asList(token)));
                                            else {
                                                guardTokens = (ArrayList<String>) snapshot.child("tokens").getValue();
                                                guardTokens.add(token);
                                                database.getReference("Shifts").child(shiftUID).child("tokens").setValue(guardTokens);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                            invalidCode = false;
                            getActivity().finish();
                            startActivity(new Intent(getContext(), MainActivity.class));
                            break;
                        }
                    if (invalidCode)
                        Toast.makeText(getActivity(), "Invalid shift code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @OnClick(R.id.currentShiftUnlinkBtn)
    public void unlinkCurrentShift() {
        userReference.child("currentShift").setValue("noshift");
        userReference.child("currentRole").setValue("noshift");

        guardsOnShift.remove(user.getUid());
        shiftReference.child("guards").setValue(guardsOnShift);

        guardTokens.remove(token);
        shiftReference.child("tokens").setValue(guardTokens);

        getActivity().finish();
        startActivity(new Intent(getContext(), MainActivity.class));
    }
}
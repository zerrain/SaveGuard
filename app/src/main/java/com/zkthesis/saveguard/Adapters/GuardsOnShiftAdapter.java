package com.zkthesis.saveguard.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zkthesis.saveguard.Activities.MainActivity;
import com.zkthesis.saveguard.Activities.ViewGuardNotesActivity;
import com.zkthesis.saveguard.Guard;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;

public class GuardsOnShiftAdapter extends RecyclerView.Adapter<GuardsOnShiftAdapter.GuardsOnShiftViewHolder> {

    private ArrayList<Guard> guards;
    protected Context context;
    private String guardRole;
    protected FirebaseDatabase database;
    protected DatabaseReference selectedGuardReference;

    public static class GuardsOnShiftViewHolder extends RecyclerView.ViewHolder {

        public TextView guardNameRVTV;
        public TextView guardRoleRVTV;


        public GuardsOnShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            guardNameRVTV = itemView.findViewById(R.id.guardNameRVTV);
            guardRoleRVTV = itemView.findViewById(R.id.guardRoleRVTV);
        }
    }

    public GuardsOnShiftAdapter(ArrayList<Guard> guards, Context context, String guardRole) {
        this.guards = guards;
        this.context = context;
        this.guardRole = guardRole;
    }

    @NonNull
    @Override
    public GuardsOnShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_guards_on_shift, parent, false);

        database = FirebaseDatabase.getInstance();
        selectedGuardReference = database.getReference("Users");

        return new GuardsOnShiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuardsOnShiftViewHolder holder, int position) {
        int guardPosition = holder.getLayoutPosition();
        selectedGuardReference = selectedGuardReference.child(guards.get(guardPosition).getuID()).child("Profile");
        holder.guardNameRVTV.setText(guards.get(guardPosition).getFullName());
        holder.guardRoleRVTV.setText(guards.get(guardPosition).getCurrentRole());

        if (guards.get(guardPosition).getCurrentRole().equals("Supervisor")) {
            holder.guardNameRVTV.setTextColor(Color.WHITE);
            holder.guardNameRVTV.setTypeface(null, Typeface.BOLD);
            holder.guardRoleRVTV.setTextColor(Color.WHITE);
            holder.guardRoleRVTV.setTypeface(null, Typeface.BOLD);
        }

        ArrayList<String> dialogOptions = new ArrayList<>();
        dialogOptions.add("View Notes");
        dialogOptions.add("View Emergency Contact");

        if (guardRole.equals("Supervisor") && guards.get(guardPosition).getCurrentRole().equals("Guard"))
            dialogOptions.add("Remove Guard");

        AlertDialog.Builder guardOptions = new AlertDialog.Builder(context);
        guardOptions.setTitle("Options");

        holder.itemView.setOnClickListener(v -> {
            if (guardRole.equals("Guard"))
                viewSelectedNotes(guardPosition);
            else if (guardRole.equals("Supervisor")) {
                String[] dialogOptionsArray = dialogOptions.toArray(new String[dialogOptions.size()]);
                guardOptions.setItems(dialogOptionsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (dialogOptionsArray[which]) {
                            case "View Notes":
                                viewSelectedNotes(guardPosition);
                                break;
                            case "View Emergency Contact":
                                viewSelectedEmergency(guardPosition);
                                break;
                            case "Remove Guard":
                                removeSelectedGuard(guardPosition);
                                break;
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return guards.size();
    }

    protected void viewSelectedNotes(int guardPosition) {
        context.startActivity(new Intent(context, ViewGuardNotesActivity.class)
                .putExtra("guard", guards.get(guardPosition)));
    }

    protected void viewSelectedEmergency(int guardPosition) {
        selectedGuardReference.child("emergencyContact")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getValue() == null)
                            Toast.makeText(context, "No contact details available", Toast.LENGTH_SHORT).show();
                        else
                            new AlertDialog.Builder(context)
                                    .setTitle("Emergency Contact")
                                    .setMessage(snapshot.child("name").getValue().toString() + "\n"
                                            + snapshot.child("number").getValue().toString() + "\n"
                                            + snapshot.child("relationship").getValue().toString())
                                    .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    protected void removeSelectedGuard(int guardPosition) {
        String currentShift = ((MainActivity) context).getCurrentShift();
        String currentGuard = guards.get(guardPosition).getuID();

        //update guard -> currentRole, currentShift
        //update shift -> tokens, guards

        selectedGuardReference.child("currentShift").setValue("noshift");
        selectedGuardReference.child("currentRole").setValue("noshift");

        ArrayList<String> guardsOnShift = new ArrayList<>();
        ArrayList<String> guardTokens = new ArrayList<>();

        DatabaseReference shiftReference = database.getReference("Shifts").child(currentShift);

        shiftReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    guardsOnShift.clear();
                    guardsOnShift.addAll((ArrayList<String>) snapshot.child("guards").getValue());
                    guardsOnShift.remove(currentGuard);
                    shiftReference.child("guards").setValue(guardsOnShift);

                    guardTokens.clear();
                    guardTokens.addAll((ArrayList<String>) snapshot.child("tokens").getValue());
                    guardTokens.remove(guards.get(guardPosition).getToken());
                    shiftReference.child("tokens").setValue(guardTokens);
                    Toast.makeText(context, "Guard" + guards.get(guardPosition).getFullName()
                            + " Removed", Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).finish();
                    context.startActivity(new Intent(context, MainActivity.class));
                }
                else
                    Toast.makeText(context, "Unable to kick guard", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

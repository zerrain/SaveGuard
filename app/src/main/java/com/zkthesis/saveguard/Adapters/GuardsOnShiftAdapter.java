package com.zkthesis.saveguard.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zkthesis.saveguard.Activities.ViewGuardNotesActivity;
import com.zkthesis.saveguard.Guard;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;

public class GuardsOnShiftAdapter extends RecyclerView.Adapter<GuardsOnShiftAdapter.GuardsOnShiftViewHolder> {

    private ArrayList<Guard> guards;
    private Context context;

    public static class GuardsOnShiftViewHolder extends RecyclerView.ViewHolder {

        public TextView guardNameRVTV;
        public TextView guardRoleRVTV;


        public GuardsOnShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            guardNameRVTV = itemView.findViewById(R.id.guardNameRVTV);
            guardRoleRVTV = itemView.findViewById(R.id.guardRoleRVTV);
        }
    }

    public GuardsOnShiftAdapter(ArrayList<Guard> guards, Context context) {
        this.guards = guards;
        this.context = context;
    }

    @NonNull
    @Override
    public GuardsOnShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_guards_on_shift, parent, false);

        return new GuardsOnShiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuardsOnShiftViewHolder holder, int position) {
        int guardPosition = holder.getLayoutPosition();
        holder.guardNameRVTV.setText(guards.get(guardPosition).getFullName());
        holder.guardRoleRVTV.setText(guards.get(guardPosition).getCurrentRole());

        if (guards.get(guardPosition).getCurrentRole().equals("Supervisor")) {
            holder.guardNameRVTV.setTextColor(Color.WHITE);
            holder.guardNameRVTV.setTypeface(null, Typeface.BOLD);
            holder.guardRoleRVTV.setTextColor(Color.WHITE);
            holder.guardRoleRVTV.setTypeface(null, Typeface.BOLD);
        }

        holder.itemView.setOnClickListener(v -> {
                context.startActivity(new Intent(context, ViewGuardNotesActivity.class)
                        .putExtra("guard", guards.get(guardPosition)));
        });

    }

    @Override
    public int getItemCount() {
        return guards.size();
    }
}

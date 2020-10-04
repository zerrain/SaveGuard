package com.zkthesis.saveguard.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zkthesis.saveguard.R;

public class ShiftsFragment extends Fragment {

    public ShiftsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shifts, container, false);
        //Toolbar toolbar = (Toolbar) v.findViewById()
        return v;
    }
}
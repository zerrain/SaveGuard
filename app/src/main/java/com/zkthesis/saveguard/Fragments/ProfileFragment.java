package com.zkthesis.saveguard.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zkthesis.saveguard.Activities.LoginActivity;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileFragment extends Fragment {

    @BindView(R.id.profileLicenceIV)
    ImageView profileLicenceIV;

    private StorageReference storageReference;
    private FirebaseUser user;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("Users/" + user.getUid() + "/licence");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);

        Glide.with(this).load(storageReference).into(profileLicenceIV);

        return v;
    }

    @OnClick(R.id.logOutButton)
    public void logUserOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getContext(), LoginActivity.class));
        getActivity().finish();
    }
}
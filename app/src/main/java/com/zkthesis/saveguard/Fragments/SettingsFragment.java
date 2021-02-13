package com.zkthesis.saveguard.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zkthesis.saveguard.Activities.LoginActivity;
import com.zkthesis.saveguard.Activities.MainActivity;
import com.zkthesis.saveguard.Activities.ViewLicenceActivity;
import com.zkthesis.saveguard.R;

import butterknife.ButterKnife;

public class SettingsFragment extends PreferenceFragmentCompat {

    private FirebaseUser user;
    protected FirebaseDatabase database;
    protected DatabaseReference userReference;

    private static final int GALLERY_REQUEST = 1;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, null);
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, parent, false);

        ButterKnife.bind(this, v);

        MaterialToolbar toolbar = getActivity().findViewById(R.id.mainToolbar);
        toolbar.getMenu().clear();

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("Users").child(user.getUid()).child("Profile");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null)
                    findPreference("profileDetailsPreference").setSummary(
                            snapshot.child("fullName").getValue().toString() + "\n" + snapshot.child("mobileNO").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setPreferenceOnClickListeners();

        return super.onCreateRecyclerView(inflater, parent, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST) {
            if (data != null)
                uploadLicenceImage(data.getData());
        }
    }

    private void setPreferenceOnClickListeners() {
        Preference viewLicencePreference = findPreference("viewLicencePreference");
        viewLicencePreference.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), ViewLicenceActivity.class));
            return false;
        });

        Preference changeLicencePreference = findPreference("changeLicencePreference");
        changeLicencePreference.setOnPreferenceClickListener(preference -> {
            showImagePicker();
            return false;
        });

        Preference changeNamePreference = findPreference("changeNamePreference");
        changeNamePreference.setOnPreferenceClickListener(preference -> {
            showNameChangeAlert();
            return false;
        });

        Preference changeMobileNOPreference = findPreference("changeMobileNOPreference");
        changeMobileNOPreference.setOnPreferenceClickListener(preference -> {
            showNumberChangeAlert();
            return false;
        });

        Preference aboutPreference = findPreference("aboutPreference");
        aboutPreference.setOnPreferenceClickListener(preference -> {
            showAboutAlert();
            return false;
        });

        Preference logOutPreference = findPreference("logOutPreference");
        logOutPreference.setOnPreferenceClickListener(preference -> {
            showLogOutAlert();
            return false;
        });
    }

    private void showImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void uploadLicenceImage(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        ProgressDialog progressDialog = new ProgressDialog(getContext());

        if (imageUri != null) {
            progressDialog.setTitle("Uploading Licence...");
            progressDialog.show();

            StorageReference childStorageRef = storageReference.child("Users/" + user.getUid() + "/licence");
            childStorageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Failed to upload licence", Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    });
        }
    }

    private void showNameChangeAlert() {
        EditText nameInputET = new EditText(getActivity());

        new AlertDialog.Builder(getActivity())
                .setTitle("Enter new name")
                .setCancelable(true)
                .setView(nameInputET)
                .setPositiveButton("Ok",
                        (dialog, which) -> {
                            String nameInput = nameInputET.getText().toString().trim();
                            if (!nameInput.isEmpty()) {
                                userReference.child("fullName").setValue(nameInput);
                                ((MainActivity) getActivity()).switchToProfileFragment();
                            } else
                                Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Cancel",
                        (dialog, which) ->
                                dialog.cancel())
                .show();
    }

    private void showNumberChangeAlert() {
        EditText numberInputET = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle("Enter new number")
                .setCancelable(true)
                .setView(numberInputET)
                .setPositiveButton("Ok",
                        (dialog, which) -> {
                            String numberInput = numberInputET.getText().toString().trim();
                            if (!numberInput.isEmpty()) {
                                userReference.child("mobileNO").setValue(numberInput);
                                ((MainActivity) getActivity()).switchToProfileFragment();
                            } else
                                Toast.makeText(getContext(), "Please enter your number", Toast.LENGTH_SHORT).show();
                        })
                .setNegativeButton("Cancel",
                        (dialog, which) ->
                                dialog.cancel())
                .show();
    }

    private void showAboutAlert() {
        new AlertDialog.Builder(getActivity())
                .setMessage("This application was developed by Zaid & Kanj for our Thesis")
                .setCancelable(true)
                .setPositiveButton("Ok",
                        (dialog, which) ->
                                dialog.cancel())
                .show();
    }

    private void showLogOutAlert() {
        new AlertDialog.Builder(getActivity())
                .setMessage("Are you sure you want to log out?")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        (dialog, which) -> {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getContext(), LoginActivity.class));
                            getActivity().finish();
                        })
                .setNegativeButton("No",
                        (dialog, which) ->
                                dialog.cancel())
                .show();
    }
}
package com.zkthesis.saveguard.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zkthesis.saveguard.Fragments.NotesFragment;
import com.zkthesis.saveguard.Fragments.SettingsFragment;
import com.zkthesis.saveguard.Fragments.ShiftsFragment;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainToolbar)
    MaterialToolbar mainToolbar;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;
    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;

    private FirebaseUser user;
    protected FirebaseDatabase database;
    private DatabaseReference userReference;
    protected ArrayList<String> guardTokens;
    protected String currentShift;

    protected String guardName;
    protected String guardRole;
    private boolean volUp = false;
    private boolean volDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userReference = database.getReference("Users").child(user.getUid()).child("Profile");

        currentShift = "noshift";
        guardName = "";
        guardTokens = new ArrayList<>();

        userReference.child("currentShift").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null || snapshot.getValue().toString().equals("noshift"))
                    currentShift = "noshift";
                else
                    currentShift = snapshot.getValue().toString().trim();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("fullName").getValue() != null) {
                    guardName = snapshot.child("fullName").getValue().toString();
                    guardRole = snapshot.child("currentRole").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        try {
            if (getIntent().getStringExtra("toNoteFrag").equals("true")) {
                mainToolbar.setTitle("Notes");
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new NotesFragment()).commit();
                bottomNavigationView.setSelectedItemId(R.id.notesFragment);
            } else {
                mainToolbar.setTitle("Shift");
                getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new ShiftsFragment()).commit();
            }
        } catch (Exception e) {
            mainToolbar.setTitle("Shift");
            getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new ShiftsFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        switch (item.getItemId()) {
            case R.id.shiftsFragment:
                switchToShiftsFragment();
                break;
            case R.id.notesFragment:
                switchToNotesFragment();
                break;
            case R.id.profileFragment:
                switchToProfileFragment();
                break;
        }
        return true;
    };

    public void switchToShiftsFragment() {
        mainToolbar.setTitle("Shift");
        replaceFragment(new ShiftsFragment());
    }

    public void switchToNotesFragment() {
        mainToolbar.setTitle("Notes");
        replaceFragment(new NotesFragment());
    }

    public void switchToProfileFragment() {
        mainToolbar.setTitle("Profile");
        replaceFragment(new SettingsFragment());
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            volDown = true;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            volUp = true;
        if (volUp && volDown)
            if (!currentShift.equals("noshift"))
                alertGuards();
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
            volDown = false;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
            volUp = false;
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    public String getCurrentShift() {
        return currentShift;
    }

    public String getGuardRole() {
        return guardRole;
    }

    private void alertGuards() {
        database.getReference("Shifts").child(currentShift).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    ArrayList<String> guardsOnShiftTokens = new ArrayList<>((ArrayList<String>) snapshot.child("tokens").getValue());
                    database.getReference("Alert").child("tokens").setValue(guardsOnShiftTokens);
                    database.getReference("Alert").child("guardName").setValue(guardName);
                    Toast.makeText(MainActivity.this, "Guards Alerted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
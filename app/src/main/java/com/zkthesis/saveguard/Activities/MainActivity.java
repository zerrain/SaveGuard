package com.zkthesis.saveguard.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.zkthesis.saveguard.Fragments.NotesFragment;
import com.zkthesis.saveguard.Fragments.ProfileFragment;
import com.zkthesis.saveguard.Fragments.ShiftsFragment;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainToolbar)
    MaterialToolbar mainToolbar;
    @BindView(R.id.fragmentContainer)
    FrameLayout fragmentContainer;
    @BindView(R.id.bottomNavigationView)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        mainToolbar.setTitle("Shifts");

        Toast.makeText(this, mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();

        getSupportFragmentManager().beginTransaction().add(R.id.fragmentContainer, new ShiftsFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        }
    };

    private void switchToShiftsFragment() {
        mainToolbar.setTitle("Shifts");
        //TODO load menu options
        replaceFragment(new ShiftsFragment());
    }

    private void switchToNotesFragment() {
        mainToolbar.setTitle("Notes");
        //TODO load menu options
        replaceFragment(new NotesFragment());
    }

    private void switchToProfileFragment() {
        mainToolbar.setTitle("Profile");
        //TODO load menu options
        replaceFragment(new ProfileFragment());
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
    }
}
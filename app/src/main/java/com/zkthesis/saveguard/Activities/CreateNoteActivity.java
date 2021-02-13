package com.zkthesis.saveguard.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zkthesis.saveguard.R;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateNoteActivity extends AppCompatActivity {

    @BindView(R.id.createNoteTitleET)
    EditText createNoteTitleET;
    @BindView(R.id.createNoteTextET)
    EditText createNoteTextET;
    @BindView(R.id.createNoteCreateBtn)
    Button createNoteCreateBtn;
    @BindView(R.id.createNoteCancelBtn)
    Button createNoteCancelBtn;

    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ButterKnife.bind(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users").child(user.getUid()).child("Profile").child("Notes").push();

        createNoteTextET.setMovementMethod(new ScrollingMovementMethod());
    }

    @OnClick(R.id.createNoteCreateBtn)
    public void createNote() {
        if (createNoteTitleET.getText().toString().trim().equals("") || createNoteTextET.getText().toString().trim().equals(""))
            Toast.makeText(this, "Complete note details", Toast.LENGTH_SHORT).show();
        else {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            reference.child("noteTitle").setValue(createNoteTitleET.getText().toString().trim());
            reference.child("noteText").setValue(createNoteTextET.getText().toString().trim());
            reference.child("dateAdded").setValue(checkDigit(day) + "/" + checkDigit(month) + "/" + year);
            reference.child("timeAdded").setValue(checkDigit(hour) + ":" + checkDigit(minute));
            reference.child("shiftTaken").setValue(getIntent().getStringExtra("currentShift"));
            finish();
            closeCreateNote();
        }


    }

    @OnClick(R.id.createNoteCancelBtn)
    public void closeCreateNote() {
        Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
        intent.putExtra("toNoteFrag", "true");
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeCreateNote();
    }

    private String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }
}
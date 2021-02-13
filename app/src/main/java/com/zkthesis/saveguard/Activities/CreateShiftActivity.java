package com.zkthesis.saveguard.Activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zkthesis.saveguard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateShiftActivity extends AppCompatActivity {

    @BindView(R.id.shiftLocationET)
    EditText shiftLocationET;
    @BindView(R.id.shiftStartTimeET)
    EditText shiftStartTimeET;
    @BindView(R.id.shiftFinishTimeET)
    EditText shiftFinishTimeET;
    @BindView(R.id.shiftDateET)
    EditText shiftDateET;

    protected FirebaseDatabase database;
    protected DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shift);
        ButterKnife.bind(this);
        setListeners();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Shifts").push();
    }

    private void setListeners() {
        shiftStartTimeET.setInputType(InputType.TYPE_NULL);
        shiftStartTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get((Calendar.MINUTE));
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateShiftActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                shiftStartTimeET.setText(checkDigit(hourOfDay) + ":" + checkDigit(minute));
                            }
                        }, hour, minutes, true);
                timePickerDialog.show();
            }
        });

        shiftFinishTimeET.setInputType(InputType.TYPE_NULL);
        shiftFinishTimeET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get((Calendar.MINUTE));
                TimePickerDialog timePickerDialog = new TimePickerDialog(CreateShiftActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        shiftFinishTimeET.setText(checkDigit(hourOfDay) + ":" + checkDigit(minute));
                    }
                }, hour, minutes, true);
                timePickerDialog.show();
            }
        });

        shiftDateET.setInputType(InputType.TYPE_NULL);
        shiftDateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = 1;
                int month = 0;
                int year = calendar.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateShiftActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                shiftDateET.setText(dayOfMonth + "/" + ++month + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    private String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    @OnClick(R.id.createShiftBtn)
    public void createShift() {
        if (shiftLocationET.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter shift location", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shiftStartTimeET.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter shift start time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shiftFinishTimeET.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter shift finish time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shiftDateET.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter shift date", Toast.LENGTH_SHORT).show();
            return;
        }

        String shiftLocation = shiftLocationET.getText().toString().trim();
        String shiftStartTime = shiftStartTimeET.getText().toString().trim();
        String shiftFinishTime = shiftFinishTimeET.getText().toString().trim();
        String shiftDate = shiftDateET.getText().toString().trim();

        reference.child("shiftLocation").setValue(shiftLocation);
        reference.child("shiftStartTime").setValue(shiftStartTime);
        reference.child("shiftFinishTime").setValue(shiftFinishTime);
        reference.child("shiftDate").setValue(shiftDate);
        reference.child("guards").setValue(new ArrayList<>(Arrays.asList(FirebaseAuth.getInstance().getCurrentUser().getUid())));
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful())
                return;
            reference.child("tokens").setValue(new ArrayList<>(Arrays.asList(task.getResult())));
        });
        database.getReference("Shifts").child("allShifts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null)
                    database.getReference("Shifts").child("allShifts").setValue(new ArrayList<>(Arrays.asList(reference.getKey())));
                else {
                    ArrayList<String> allShifts = new ArrayList<>((ArrayList<String>) snapshot.getValue());
                    allShifts.add(reference.getKey());
                    database.getReference("Shifts").child("allShifts").setValue(allShifts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser()
                .getUid()).child("Profile").child("currentShift").setValue(reference.getKey());

        startActivity(new Intent(CreateShiftActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CreateShiftActivity.this, MainActivity.class));
        finish();
    }
}
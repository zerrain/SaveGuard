package com.zkthesis.saveguard.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ResetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.resetPwdEmailEditText)
    EditText resetPwdEmailEditText;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private static String TAG = "ResetPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @OnClick(R.id.resetPwdBtn)
    public void resetPassword() {
        String email = resetPwdEmailEditText.getText().toString().trim();
        hideKeyboard();
        if(isValidEmail(email)) {
            progressDialog.setMessage("Loading...");
            progressDialog.show();
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email successfully sent");
                                Toast.makeText(ResetPasswordActivity.this, "Password reset E-mail sent", Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Failed to send E-mail", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else {
            Toast.makeText(ResetPasswordActivity.this, "Valid E-mail required", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
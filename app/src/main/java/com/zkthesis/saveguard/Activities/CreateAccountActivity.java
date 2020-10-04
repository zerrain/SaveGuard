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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateAccountActivity extends AppCompatActivity {

    @BindView(R.id.createAccEmailEditText)
    EditText createAccEmailEditText;
    @BindView(R.id.createAccPwdEditText)
    EditText createAccPwdEditText;
    @BindView(R.id.createEmailInputLayout)
    TextInputLayout createEmailInputLayout;
    @BindView(R.id.createPwdInputLayout)
    TextInputLayout createPwdInputLayout;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private static final String TAG = "CreateAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        createEmailInputLayout.setHintEnabled(false);
        createPwdInputLayout.setHintEnabled(false);

        progressDialog = new ProgressDialog(this);
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isValidPassword(CharSequence target) {
        return target.length() >= 6;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void checkExceptions(Task<AuthResult> task) {
        String exception = task.getException().getClass().toString();
        if (exception.equals(FirebaseAuthUserCollisionException.class.toString())) {
            Log.d(TAG, task.getException().toString());
            Toast.makeText(CreateAccountActivity.this, "E-mail already exists", Toast.LENGTH_LONG).show();
        } else if (exception.equals(FirebaseNetworkException.class.toString())) {
            Log.d(TAG, task.getException().toString());
            Toast.makeText(this, "Please check your network", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.createAccBtn)
    public void createAccount() {
        String email = createAccEmailEditText.getText().toString().trim();
        String password = createAccPwdEditText.getText().toString().trim();
        hideKeyboard();

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid E-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be 6 or more characters", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.setMessage("Creating account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail: success");
                            sendVerificationEmail();
                            DatabaseReference reference = FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            reference.child("Profile").child("setupCompleted").setValue(false);
                        } else {
                            checkExceptions(task);
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        firebaseAuth.getCurrentUser()
                .sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Authentication email sent successfully " + task.getResult());
                            Toast.makeText(CreateAccountActivity.this, "Verification E-mail sent", Toast.LENGTH_LONG).show();
                            createAccEmailEditText.setText("");
                            createAccPwdEditText.setText("");
                            finish();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            Log.d(TAG, "Authentication email failed to send " + task.getException());
                            Toast.makeText(CreateAccountActivity.this, "Account creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
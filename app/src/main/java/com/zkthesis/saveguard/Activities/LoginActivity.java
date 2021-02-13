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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.googleSignInButton)
    SignInButton googleSignInButton;
    @BindView(R.id.loginEmailET)
    EditText loginEmailET;
    @BindView(R.id.loginEmailTIL)
    TextInputLayout loginEmailTIL;
    @BindView(R.id.loginPasswordET)
    EditText loginPasswordET;
    @BindView(R.id.loginPasswordTIL)
    TextInputLayout loginPasswordTIL;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    protected FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private GoogleSignInClient signInClient;
    protected ProgressDialog progressDialog;

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) { }

        user = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (user != null && userIsVerified()) {
            if (!LoginActivity.this.isFinishing()) {
                progressDialog.setMessage("Logging in");
                progressDialog.show();
            }
            Log.d(TAG, "User is currently signed in: " + user.getEmail());
            setupCompletedCheck();
        }

        loginEmailTIL.setHintEnabled(false);
        loginPasswordTIL.setHintEnabled(false);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        configureGoogleClient();
    }

    protected void googleSignIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void configureGoogleClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Log.d(TAG, "signInWithCredential:success: currentUser: " + user.getEmail());
                            setupCompletedCheck();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Sign In failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean userIsVerified() {
        if (firebaseAuth.getCurrentUser() != null) {
            String email = firebaseAuth.getCurrentUser().getEmail();
            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                Log.d(TAG, email + " has been verified");
                firebaseAuth.getCurrentUser().reload();
                return true;
            } else {
                Log.d(TAG, email + " has not been verified");
                Snackbar.make(findViewById(R.id.loginLayout),
                        email + " has not been verified yet, check your inbox or resend a verification email", 5000)
                        .setAction("Resend", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "Send verification button clicked");
                                sendVerificationEmail();
                            }
                        }).show();
                return false;
            }
        }
        return false;
    }

    protected void sendVerificationEmail() {
        firebaseAuth.getCurrentUser()
                .sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Authentication email sent successfully " + task.getResult());
                            Toast.makeText(LoginActivity.this, "Verification E-mail sent", Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(TAG, "Authentication email failed to send " + task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to send verification E-mail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Hides the keyboard if a text field is focused
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    /**
     * See how Butter Knife also lets us add an on click event by adding this annotation before the
     * declaration of the function, making our life way easier.
     */
    // Authenticates the email and password with Firebase
    @OnClick(R.id.loginBtn)
    public void logIn() {
        String email = loginEmailET.getText().toString();
        String password = loginPasswordET.getText().toString();
        hideKeyboard();

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid E-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in");
        progressDialog.show();

        //Will need to work on logging in with username as well + credential validation
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail: sucess");
                            if (userIsVerified())
                                setupCompletedCheck();
                        } else {
                            Log.d(TAG, "signInWithEmail: failure", task.getException());
                            String invalidUser = "com.google.firebase.auth.FirebaseAuthInvalidUserException";
                            String invalidCredentials = "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException";
                            String exceptionString = task.getException().toString();
                            if (exceptionString.startsWith(invalidUser) || exceptionString.startsWith(invalidCredentials)) {
                                Toast.makeText(LoginActivity.this, "You have entered an invalid username or password",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "An error occurred during logging in, please try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Having a tag, and the name of the function on the console message helps allot in
        // knowing where the message should appear.
        Log.d(TAG, "username: " + email + " password: " + password);
    }

    protected void setupCompletedCheck() {
        progressDialog.dismiss();

        user = FirebaseAuth.getInstance().getCurrentUser();

        reference = database.getReference("Users").child(user.getUid()).child("Profile");
        reference.keepSynced(true);

        reference.child("setupCompleted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if ((boolean) dataSnapshot.getValue() && user != null)
                        launchMainActivity();
                    else
                        launchSetupActivity();
                } catch (Exception e) {
                    launchSetupActivity();
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    protected void launchMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    protected void launchSetupActivity() {
        startActivity(new Intent(LoginActivity.this, SetupAccountActivity.class));
        finish();
    }

    @OnClick(R.id.createAccBtn)
    public void launchCreateAccount() {
        startActivity(new Intent(this, CreateAccountActivity.class));
    }

    @OnClick(R.id.resetPwdBtn)
    public void launchResetPassword() {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }
}
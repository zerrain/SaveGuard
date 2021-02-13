package com.zkthesis.saveguard.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zkthesis.saveguard.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SetupAccountActivity extends AppCompatActivity {

    @BindView(R.id.fullNameSetupET)
    EditText fullNameSetupET;
    @BindView(R.id.mobileNoSetupET)
    EditText mobileNoSetupET;
    @BindView(R.id.licenceUploadPreviewIV)
    ImageView licenceUploadPreviewIV;

    private Uri imageUri = null;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_account);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users").child(user.getUid());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST) {
            try {
                if (data != null) {
                    imageUri = data.getData();
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);
                    licenceUploadPreviewIV.setImageBitmap(selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.licenceUploadBtn)
    public void onLicenceUploadBtnClicked() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @OnClick(R.id.completeSetupBtn)
    public void onCompleteSetupBtnClicked() {
        String fullName = fullNameSetupET.getText().toString().trim();
        String mobileNO = mobileNoSetupET.getText().toString().trim();

        if (fullName.isEmpty() || mobileNO.isEmpty() || imageUri == null)
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
        else {
            reference.child("Profile").child("fullName").setValue(fullName);
            reference.child("Profile").child("mobileNO").setValue(mobileNO);
            reference.child("Profile").child("setupCompleted").setValue(true);
            reference.child("Profile").child("currentShift").setValue("noshift");
            uploadLicenceImage();

            Toast.makeText(this, "Setup Complete!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

            startActivity(new Intent(SetupAccountActivity.this, MainActivity.class));
            finish();
        }
    }

    private void uploadLicenceImage() {
        if (imageUri != null) {
            progressDialog.setTitle("Uploading Licence...");
            progressDialog.show();

            StorageReference childStorageRef = storageReference.child("Users/" + user.getUid() + "/licence");
            childStorageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SetupAccountActivity.this, "Failed to upload licence", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }
}
package com.zkthesis.saveguard.Activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zkthesis.saveguard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewLicenceActivity extends AppCompatActivity {

    @BindView(R.id.viewLicenceIV)
    ImageView viewLicenceIV;
    @BindView(R.id.imageLoadProgressBar)
    ProgressBar imageLoadProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_licence);
        ButterKnife.bind(this);

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/licence");

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this).load(uri).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    imageLoadProgressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(viewLicenceIV);
        });
    }

}
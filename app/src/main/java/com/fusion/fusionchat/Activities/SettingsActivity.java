package com.fusion.fusionchat.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.camera.core.internal.utils.ImageUtil;

import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private String profilePicUrl ="https://firebasestorage.googleapis.com/v0/b/fusion-chat-official.appspot.com/o/profile_pictures%2Fdefault_profile_pic.png?alt=media&token=9a165c85-4792-48ad-8752-2d8f750d10d7&_gl=1*rkcn4s*_ga*MTEwMzY4MDE3MC4xNjc5OTE1MTc5*_ga_CW55HF8NVT*MTY5NjUxODIyNS43MC4xLjE2OTY1MjI2OTQuNjAuMC4w";
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> mTakePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Setting the old details of user before update
        setOldDetails();

        binding.updatedPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakePic.launch("image/*");
            }
        });


        //Logic to upload the image on FirebaseStorage , It may take some time
        mTakePic = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                binding.updatedPic.setImageURI(result);

                final StorageReference reference = storage.getReference()
                        .child("profile_pictures")
                        .child(FirebaseAuth.getInstance().getUid());
                reference.putFile(result)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                binding.submitBtn.setVisibility(View.INVISIBLE);
                                binding.loading.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Image uploaded successfully
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        profilePicUrl = uri.toString();
                                        binding.submitBtn.performClick();
                                        binding.submitBtn.setVisibility(View.VISIBLE);
                                        binding.loading.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(exception -> {
                            // Handle failed image upload
                            Toast.makeText(SettingsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Now updating on btn click
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNewDetails();
            }
        });

    }

    private void setOldDetails() {
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Users user = snapshot.getValue(Users.class);

                            String previousName = user.getUsername();
                            binding.updatedName.setText(previousName);

                            String about = user.getAbout();
                            binding.about.setText(about);


                             profilePicUrl = user.getProfilePicResId();
                            Picasso.get().load(profilePicUrl).placeholder(R.drawable.default_profile_pic).into(binding.updatedPic);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    private void updateNewDetails() {
        String updatedName = binding.updatedName.getText().toString();
        String about = binding.about.getText().toString();
        if (updatedName.isEmpty())
            binding.updatedName.setError("Enter Username");
        else {
            Map<String, Object> obj = new HashMap<>();
            obj.put("username", updatedName);
            obj.put("about", about);
            obj.put("profilePicResId", profilePicUrl);
            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                    .updateChildren(obj);
        }

    }

    private void switchThemeMode() {
        try {
            int nightMode = AppCompatDelegate.getDefaultNightMode();
            if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
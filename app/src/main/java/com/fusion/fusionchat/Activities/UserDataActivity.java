package com.fusion.fusionchat.Activities;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityUserDataBinding;
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
import com.shashank.sony.fancytoastlib.FancyToast;
import com.squareup.picasso.Picasso;
public class UserDataActivity extends AppCompatActivity {
    private ActivityUserDataBinding binding;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ActivityResultLauncher<String> mTakePic;
    private String photoUrl = "https://firebasestorage.googleapis.com/v0/b/fusion-chat-official.appspot.com/o/profile_pictures%2Fdefault_profile_pic.png?alt=media&token=9a165c85-4792-48ad-8752-2d8f750d10d7&_gl=1*rkcn4s*_ga*MTEwMzY4MDE3MC4xNjc5OTE1MTc5*_ga_CW55HF8NVT*MTY5NjUxODIyNS43MC4xLjE2OTY1MjI2OTQuNjAuMC4w";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

       // startDynamicBG();
        setOldDetails(); //If user have already setup profile before

        String number = getIntent().getStringExtra("number");


        binding.firstProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakePic.launch("image/*");
            }
        });
        binding.plusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakePic.launch("image/*");
            }
        });

        //Logic to upload the image on FirebaseStorage , It may take some time
        mTakePic = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                binding.firstProfilePic.setImageURI(result);

                final StorageReference reference = storage.getReference().child("profile_pictures")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                         reference.putFile(result)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                binding.goBtn.setVisibility(View.INVISIBLE);
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
                                        photoUrl = uri.toString();
                                        binding.goBtn.setVisibility(View.VISIBLE);
                                       binding.loading.setVisibility(View.INVISIBLE);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(exception -> {
                            FancyToast.makeText(getApplicationContext(), "Failed", FancyToast.LENGTH_LONG, FancyToast.ERROR, true);
                            // Handle failed image upload
                            binding.goBtn.setVisibility(View.VISIBLE);
                           binding.loading.setVisibility(View.INVISIBLE);
                        });

            }
        });

        binding.goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.firstUserName.getText().toString();
                String about = binding.firstAbout.getText().toString();
                uploadUsersData(username, number, photoUrl, about);
            }
        });

    }

    private void startDynamicBG() {
        AnimationDrawable drawable = (AnimationDrawable) binding.userDataActivityLayout.getBackground();
        drawable.setEnterFadeDuration(1000);
        drawable.setExitFadeDuration(1000);
        drawable.start();
    }

    private void uploadUsersData(String username, String number, String profilePicUrl, String about) {
        if (username.isEmpty())
            binding.firstUserName.setError("Enter Username");
        else {
            Users users = new Users(username, number, profilePicUrl, about);
            database.getReference()
                    .child("Users")
                    .child(FirebaseAuth
                            .getInstance().getCurrentUser().getUid())
                    .setValue(users);
            Intent intent = new Intent(UserDataActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }


    private void setOldDetails() {

        binding.goBtn.setVisibility(View.INVISIBLE);
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);

                    String previousName = user.getUsername();
                    binding.firstUserName.setText(previousName);

                    String about = user.getAbout();
                    binding.firstAbout.setText(about);

                    String profilePicUrl = user.getProfilePicResId();

                    Picasso.get().load(profilePicUrl).placeholder(R.drawable.default_profile_pic).into(binding.firstProfilePic);
                    photoUrl = profilePicUrl;

                    binding.firstUserName.setHint("Name");
                    binding.firstAbout.setHint("About Section");
                }
                    binding.nameLoading.setVisibility(View.INVISIBLE);
                    binding.aboutLoading.setVisibility(View.INVISIBLE);
                     binding.goBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FancyToast.makeText(getApplicationContext(), "Error!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }
        });

    }


}

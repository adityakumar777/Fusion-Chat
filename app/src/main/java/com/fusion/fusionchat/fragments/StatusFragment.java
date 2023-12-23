package com.fusion.fusionchat.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fusion.fusionchat.Adapters.StatusAdapter;
import com.fusion.fusionchat.Models.Status;
import com.fusion.fusionchat.Models.UserStatus;
import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.FragmentStatusBinding;
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

import java.util.ArrayList;
import java.util.HashMap;


public class StatusFragment extends Fragment {

     FragmentStatusBinding binding;
     ActivityResultLauncher<String> mTakePic;
     FirebaseStorage storage;
     FirebaseDatabase database;
     ArrayList<UserStatus> userStatuses = new ArrayList<>();
     Users user;
     StatusAdapter statusAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(getLayoutInflater());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        statusAdapter = new StatusAdapter(getActivity(), userStatuses);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.statusList.setLayoutManager(layoutManager);
        binding.statusList.setAdapter(statusAdapter);

        setUserValue();
        setStatuses();



        binding.myStatus.setOnClickListener(c->{
                mTakePic.launch("image/*");
        });



        mTakePic = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                if (result!=null){
                    binding.myStatus.setVisibility(View.INVISIBLE);
                    binding.loading.setVisibility(View.VISIBLE);
                }

                final StorageReference reference = storage.getReference()
                        .child("status")
                        .child(System.currentTimeMillis() + "");
                reference.putFile(result)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        UserStatus userStatus = new UserStatus();
                                        userStatus.setName(user.getUsername());
                                        userStatus.setProfileImage(user.getProfilePicResId());
                                        userStatus.setLastUpdated(System.currentTimeMillis());

                                        HashMap<String, Object> obj = new HashMap<>();
                                        obj.put("name", userStatus.getName());
                                        obj.put("profileImage", userStatus.getProfileImage());
                                        obj.put("lastUpdated", userStatus.getLastUpdated());

                                        String imageUrl = uri.toString();
                                        Status status = new Status(imageUrl, userStatus.getLastUpdated());

                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .updateChildren(obj);

                                        database.getReference().child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("statuses")
                                                .push()
                                                .setValue(status);

                                        //  setUserValue();
                                        setStatuses();
                                        binding.loading.setVisibility(View.INVISIBLE);
                                        binding.myStatus.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(exception -> {
                            binding.myStatus.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.INVISIBLE);

                        });

            }
        });

        binding.deleteStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Status");
                builder.setMessage("Are you sure you want to delete previous status ?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        database.getReference()
                                .child("stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child("statuses")
                                .setValue(null);
                        statusAdapter.notifyDataSetChanged();

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User canceled the deletion
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        return binding.getRoot();

    }

    private void setUserValue() {
        database.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(Users.class);
                        Picasso.get().load(user.getProfilePicResId()).placeholder(R.drawable.default_profile_pic).into(binding.myStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setStatuses() {

        database.getReference()
                .child("stories")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();

                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        if (statuses.size()!=0){
                        userStatuses.add(status);

                        }
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}
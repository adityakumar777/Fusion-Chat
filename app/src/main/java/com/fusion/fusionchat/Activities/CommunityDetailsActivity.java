package com.fusion.fusionchat.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.fusion.fusionchat.Adapters.UsersAdapter;
import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityCommunityDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommunityDetailsActivity extends AppCompatActivity {

    ActivityCommunityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        String creatorUid = getIntent().getStringExtra("creatorUid");
        String communityId = getIntent().getStringExtra("communityId");
        String communityName = getIntent().getStringExtra("communityName");
        String communityPic = getIntent().getStringExtra("communityPicResId");

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(creatorUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        binding.creatorName.setText(users.getUsername());
                        Picasso.get().load(users.getProfilePicResId()).placeholder(R.drawable.default_profile_pic).into(binding.creatorPic);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        ArrayList<Users> list = new ArrayList<>();
        UsersAdapter adapter = new UsersAdapter(this, list, creatorUid, communityId);
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.membersRecyclerView.setAdapter(adapter);

        binding.cmntyName.setText(communityName);
        Picasso.get().load(communityPic).placeholder(R.drawable.default_profile_pic).into(binding.cmntyPic);

        binding.backArrow.setOnClickListener(click -> {
            onBackPressed();
        });

        FirebaseDatabase.getInstance().getReference()
                .child("Community")
                .child(creatorUid)
                .child(communityId)
                .child("members")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String uid = dataSnapshot.getValue(String.class);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(uid)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Users users = snapshot.getValue(Users.class);
                                            if (!(uid.equals(creatorUid)))
                                                list.add(users);
                                            adapter.notifyDataSetChanged();

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
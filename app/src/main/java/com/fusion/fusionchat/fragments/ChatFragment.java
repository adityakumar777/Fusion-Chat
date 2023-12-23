package com.fusion.fusionchat.fragments;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fusion.fusionchat.Activities.ChatBotActivity;
import com.fusion.fusionchat.Adapters.UsersAdapter;

import com.fusion.fusionchat.Database.MyDatabaseHelper;
import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.databinding.FragmentChatBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private FirebaseDatabase database;
    private  ArrayList<Users> list= new ArrayList<>();
    MyDatabaseHelper myDB;
    private UsersAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(getLayoutInflater());

        database = FirebaseDatabase.getInstance();

        if (isNetworkAvailable()) {
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.startShimmerAnimation();
            loadUsersInChats();
        }
        else {

            FancyToast.makeText(getActivity(), "Network is not available!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }

        adapter = new UsersAdapter(getActivity(), list);
        binding.chatRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.chatRecyclerview.setAdapter(adapter);


        binding.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatBotActivity.class));
            }
        });

        return binding.getRoot();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void loadUsersInChats() {
        database.getReference().child("Users")
                .addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                               list.clear();
                                               for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                   Users users = dataSnapshot.getValue(Users.class);
                                                   users.setUsersID(dataSnapshot.getKey());
                                                 // if (!(users.getUsersID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
                                                       binding.shimmer.stopShimmerAnimation();
                                                       binding.shimmer.setVisibility(View.INVISIBLE);
                                                       list.add(users);
                                              //     }
                                               }
                                               adapter.notifyDataSetChanged();
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError error) {
                                               FancyToast.makeText(getActivity(), error.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                           }
                                       }
                );
    }

}
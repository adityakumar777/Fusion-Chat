package com.fusion.fusionchat.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.fusion.fusionchat.Adapters.CommunityAdapter;
import com.fusion.fusionchat.Models.Community;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.FragmentCommunityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;


public class CommunityFragment extends Fragment {

    private FirebaseDatabase database;
    private FragmentCommunityBinding binding;
    private CommunityAdapter adapter;
    private ActivityResultLauncher<String> mTakePic;
    private FirebaseStorage storage;
    private ArrayList<Community> list = new ArrayList<>();
    private String communityPicRedId="https://firebasestorage.googleapis.com/v0/b/fusion-chat-official.appspot.com/o/profile_pictures%2Fdefault_profile_pic.png?alt=media&token=9a165c85-4792-48ad-8752-2d8f750d10d7&_gl=1*rkcn4s*_ga*MTEwMzY4MDE3MC4xNjc5OTE1MTc5*_ga_CW55HF8NVT*MTY5NjUxODIyNS43MC4xLjE2OTY1MjI2OTQuNjAuMC4w";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(getLayoutInflater());
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        if (isNetworkAvailable()) {
            binding.shimmerCommunity.setVisibility(View.VISIBLE);
            binding.shimmerCommunity.startShimmerAnimation();
            loadCommunities();
        }
        else {
            FancyToast.makeText(getActivity(), "Network is not available!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
        }

        adapter = new CommunityAdapter(getActivity(),list);
        binding.communityRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.communityRecyclerView.setLayoutManager(layoutManager);
        binding.communityRecyclerView.setAdapter(adapter);





        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.CustomAlertDialogTheme);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.create_community_dialog, null);

        ImageView communityPic = dialogView.findViewById(R.id.community_pic);
        ImageView backBtn = dialogView.findViewById(R.id.back_arrow);
        LottieAnimationView loading = dialogView.findViewById(R.id.loading);
        TextView communityName = dialogView.findViewById(R.id.community_name);
        Button createBtn = dialogView.findViewById(R.id.create_btn);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);

        mTakePic = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                communityPic.setImageURI(result);
                loading.setVisibility(View.VISIBLE);
                createBtn.setVisibility(View.INVISIBLE);

                StorageReference reference = storage.getReference().child("community_pics/")
                        .child(System.currentTimeMillis() + "");
                reference.putFile(result)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                createBtn.setVisibility(View.INVISIBLE);
                                loading.setVisibility(View.VISIBLE);
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    communityPicRedId = uri.toString();
                                    createBtn.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }

                });
            }
        });


        binding.communityFlaotingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.show();

                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        communityName.setText("");
                        communityPic.setImageResource(R.drawable.default_profile_pic);
                        alertDialog.dismiss();
                    }
                });

                    communityPic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTakePic.launch("image/*");

                    }
                });

                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (communityName.getText().toString().isEmpty())
                            communityName.setError("Enter the Community Name");
                        else {
                          createCommunity(communityName.getText().toString());
                          communityName.setText("");
                          communityPic.setImageResource(R.drawable.default_profile_pic);
                          alertDialog.dismiss();
                        }
                    }
                });

            }
        });


        return binding.getRoot();
    }
    private void loadCommunities() {
        FirebaseDatabase.getInstance().getReference()
                .child("Community")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        if (snapshot.exists()) {
                            for (DataSnapshot datasnapshot :
                                    snapshot.getChildren()) {
                                for (DataSnapshot snap: datasnapshot.getChildren()) {
                                    Community community = snap.getValue(Community.class);
                                    binding.shimmerCommunity.stopShimmerAnimation();
                                    binding.shimmerCommunity.setVisibility(View.INVISIBLE);
                                    list.add(community);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void createCommunity(String communityName) {

        String randomKey = database.getReference().push().getKey();
        Community community = new Community(communityName, communityPicRedId);
        community.setCommunityId(randomKey);
        community.setCreatorUid(FirebaseAuth.getInstance().getUid());
        ArrayList<String> list1 = new ArrayList<>();
        list1.add(FirebaseAuth.getInstance().getUid());
        community.setMembers(list1);
        database.getReference().child("Community")
                .child(FirebaseAuth.getInstance().getUid())
                .child(randomKey)
                .setValue(community);

    }


}

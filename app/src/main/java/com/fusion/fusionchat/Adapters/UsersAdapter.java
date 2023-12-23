package com.fusion.fusionchat.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fusion.fusionchat.Activities.ChatScreenActivity;
import com.fusion.fusionchat.Activities.CommunityDetailsActivity;
import com.fusion.fusionchat.Activities.MainActivity;
import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.SampleChatLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ProfileViewHolder> {

    private ArrayList<Users> users;
    private Context context;
    private String creatorUidOfCommunity;
    private String communityID;

    public UsersAdapter(Context context, ArrayList<Users> users) {
        this.context = context;
        this.users = users;
    }
  public UsersAdapter(Context context, ArrayList<Users> users,String creatorUidOfCommunity,String communityID) {
        this.context = context;
        this.users = users;
        this.creatorUidOfCommunity = creatorUidOfCommunity;
        this.communityID = communityID;
    }


    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_chat_layout, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {

        Users profile = users.get(position);

        YoYo.with(Techniques.FadeIn)
                .duration(700)
                .playOn(holder.itemView);

        holder.binding.sampleUserName.setText(profile.getUsername());
        Picasso.get().load(profile.getProfilePicResId()).placeholder(R.drawable.default_profile_pic).into(holder.binding.sampleUserPic);

        if (context instanceof CommunityDetailsActivity){

            holder.binding.lastMessage.setText("");
            holder.binding.lastMessageTime.setText("");

            if (FirebaseAuth.getInstance().getUid().equals(creatorUidOfCommunity)) {
                holder.binding.deleteMember.setVisibility(View.VISIBLE);
            }
            holder.binding.deleteMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Remove User");
                    builder.setMessage("Are you sure you want to remove : " + profile.getUsername() + "?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ArrayList<String> list = new ArrayList<>();

                            list.add(FirebaseAuth.getInstance().getUid());

                            for (int i =0;i<users.size();i++){
                                list.add(users.get(i).getUsersID());
                            }

                            Map<String,Object> obj = new HashMap<>();
                            obj.put("members",list);

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Community")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child(communityID)
                                    .updateChildren(obj);


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

        }


        if (context instanceof  MainActivity){
        FirebaseDatabase.getInstance().getReference().child("chats")
                        .child(FirebaseAuth.getInstance().getUid()+profile.getUsersID())
                                .orderByChild("timestamp")
                                        .limitToLast(1)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.hasChildren()){
                                                            for (DataSnapshot datasnapshot: snapshot.getChildren()) {
                                                                holder.binding.lastMessage.setText(datasnapshot.child("message").getValue().toString());
                                                                Long time = Long.parseLong(datasnapshot.child("time").getValue().toString());
                                                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                                                                String formattedTime = timeFormat.format(time);
                                                                holder.binding.lastMessageTime.setText(formattedTime);
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatScreenActivity.class);
                intent.putExtra("username",users.get(position).getUsername());
                intent.putExtra("userID",users.get(position).getUsersID());
                intent.putExtra("profilePicResId",users.get(position).getProfilePicResId());
                intent.putExtra("token",users.get(position).getToken());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                Activity activity = (Activity) context;
                activity.startActivity(intent);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        }

        holder.binding.sampleUserPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfilePictureDialog(profile.getProfilePicResId(),profile.getUsername(),profile.getAbout());
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        SampleChatLayoutBinding binding;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
           binding = SampleChatLayoutBinding.bind(itemView);
        }
    }

    public void showProfilePictureDialog(String profilePicResId ,String username,String about) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.pop_profile_pic, null);

        ImageView enlargedImageView = dialogView.findViewById(R.id.popUpProfilePic);
        TextView enlargedTextView = dialogView.findViewById(R.id.popUpName);

        enlargedTextView.setText(username);
        Picasso.get().load(profilePicResId).resize(200,250).placeholder(R.drawable.default_profile_pic).into(enlargedImageView);


        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Setting the alert dialog parameters by some adjustments
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = 800; // Set the width of the dialog
        alertDialog.getWindow().setAttributes(layoutParams);

        // By clicking on image again user will dismiss the dialog
        enlargedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

}
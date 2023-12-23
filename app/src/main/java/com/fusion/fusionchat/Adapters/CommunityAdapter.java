package com.fusion.fusionchat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fusion.fusionchat.Activities.CommunityChatScreen;
import com.fusion.fusionchat.Models.Community;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.SampleCommunityLayoutBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    Context context;
    ArrayList<Community> list;

    public CommunityAdapter(Context context, ArrayList<Community> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CommunityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_community_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunityAdapter.ViewHolder holder, int position) {

        Community community = list.get(position);


        Picasso.get().load(community.getCommunityPicResId()).placeholder(R.drawable.default_profile_pic).into(holder.binding.sampleCommunityPic);
        holder.binding.sampleCommunityName.setText(community.getCommunityName());
        holder.binding.sampleMembers.setText("+"+community.getMembers().size()+" Members");

        if (community.getCreatorUid().equals(FirebaseAuth.getInstance().getUid())){
            holder.binding.deleteCommunity.setVisibility(View.VISIBLE);
            holder.binding.joinBtn.setVisibility(View.INVISIBLE);
            holder.binding.deleteCommunity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Community");
                    builder.setMessage("Are you sure you want to delete the community: " + community.getCommunityName() + "?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Community")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child(community.getCommunityId())
                                    .setValue(null);
                            holder.binding.deleteCommunity.setVisibility(View.GONE);
                            holder.binding.joinBtn.setVisibility(View.VISIBLE);
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

        if (community.getMembers().contains(FirebaseAuth.getInstance().getUid()))
        {
            holder.binding.joinBtn.setText("Joined!");
            holder.binding.joinBtn.setBackgroundResource(R.drawable.bg_sender_chatbot); ;
            holder.binding.joinBtn.setClickable(false);
        }

        holder.binding.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.binding.joinBtn.getText().equals("Join")){
                Map<String, Object> obj = new HashMap<>();
                ArrayList<String> memList = community.getMembers();
                memList.add(FirebaseAuth.getInstance().getUid());
                obj.put("members",memList);
                    FirebaseDatabase.getInstance().getReference()
                            .child("Community")
                            .child(community.getCreatorUid())
                            .child(community.getCommunityId())
                            .updateChildren(obj);
                }
            }
        });

        holder.binding.communityCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.binding.joinBtn.getText().equals("Joined!")) {
                    Intent intent = new Intent(context, CommunityChatScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("communityName", community.getCommunityName());
                    intent.putExtra("communityPicResId", community.getCommunityPicResId());
                    intent.putExtra("creatorUid", community.getCreatorUid());
                    intent.putExtra("communityId", community.getCommunityId());
                    intent.putStringArrayListExtra("members", community.getMembers());
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SampleCommunityLayoutBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleCommunityLayoutBinding.bind(itemView);
        }
    }
}

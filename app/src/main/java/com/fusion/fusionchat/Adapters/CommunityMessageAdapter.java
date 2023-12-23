package com.fusion.fusionchat.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fusion.fusionchat.Models.MessageModel;
import com.fusion.fusionchat.Models.Users;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.SampleCommunityReceiverChatBinding;
import com.fusion.fusionchat.databinding.SampleCommunitySenderChatBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CommunityMessageAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    String recID;
    String creatorUid,communityId;

    public CommunityMessageAdapter(ArrayList<MessageModel> arrayList, Context context,String creatorUid,String communityId) {
        this.messageModels = arrayList;
        this.context = context;
        this.communityId=communityId;
        this.creatorUid=creatorUid;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_community_sender_chat, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_community_receiver_chat, parent, false);
            return new ReceiverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);


        int reactions[] = new int[]{

                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {


            if (pos < 0 ){
                return false;
            }

            if (holder.getClass() == SenderViewHolder.class) {
                SenderViewHolder viewHolder = (SenderViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }

            messageModel.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("Community")
                    .child(creatorUid)
                    .child(communityId)
                    .child("messages")
                    .child(messageModel.getMessageId())
                    .setValue(messageModel);


            return true; // true is closing popup, false is requesting a new selection
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure want to delete the message?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Community")
                                        .child(creatorUid)
                                        .child(communityId)
                                        .child("messages")
                                        .child(messageModel.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return false;
            }
        });

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.senderChat.setText(messageModel.getMessage());
            viewHolder.binding.cons.setBackgroundResource(R.drawable.bg_sender_community);
            viewHolder.binding.senderTime.setText(formatTime(messageModel.getTime()));


            database.getReference().child("Users")
                    .child(messageModel.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                      if (snapshot.exists()){
                          Users user = snapshot.getValue(Users.class);
                          viewHolder.binding.usernameInChat.setText(user.getUsername());
                          Picasso.get().load(user.getProfilePicResId()).placeholder(R.drawable.default_profile_pic).into(viewHolder.binding.profilePicInChat);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });


            if(messageModel.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.senderChat.setVisibility(View.GONE);
                viewHolder.binding.cons.setBackgroundResource(R.drawable.corners);
                Picasso.get().load(messageModel.getPhoto()).placeholder(R.drawable.placeholder_img).into(viewHolder.binding.image);
            }



            if (messageModel.getFeeling() >= 0) {
                int pos = messageModel.getFeeling();
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.senderChat.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    popup.onTouch(v, event);
                    return true;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    popup.onTouch(v, event);
                    return true;
                }
            });


        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.receiverChat.setText(messageModel.getMessage());
            viewHolder.binding.receiverTime.setText(formatTime(messageModel.getTime()));

            database.getReference().child("Users")
                    .child(messageModel.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                Users user = snapshot.getValue(Users.class);
                                viewHolder.binding.usernameInChat.setText(user.getUsername());
                                Picasso.get().load(user.getProfilePicResId()).placeholder(R.drawable.default_profile_pic).into(viewHolder.binding.profilePicInChat);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

            if(messageModel.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.receiverChat.setVisibility(View.GONE);
                Picasso.get().load(messageModel.getPhoto()).placeholder(R.drawable.placeholder_img).into(viewHolder.binding.image);
            }


            if (messageModel.getFeeling() >= 0) {
                int pos = messageModel.getFeeling();
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            viewHolder.binding.receiverChat.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return true;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return true;
                }
            });
        }
        if (position == messageModels.size() -1 ){

            YoYo.with(Techniques.RollIn)
                    .duration(500)
                    .playOn(holder.itemView);
        }

    }



    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid()))
            return SENDER_VIEW_TYPE;
        else
            return RECEIVER_VIEW_TYPE;
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        SampleCommunityReceiverChatBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleCommunityReceiverChatBinding.bind(itemView);

        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        SampleCommunitySenderChatBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleCommunitySenderChatBinding.bind(itemView);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }
}


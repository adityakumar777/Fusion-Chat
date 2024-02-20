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
import com.fusion.fusionchat.Activities.CommunityChatScreen;
import com.fusion.fusionchat.Models.MessageModel;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.DeleteDialogBinding;
import com.fusion.fusionchat.databinding.SampleReceiverChatBinding;
import com.fusion.fusionchat.databinding.SampleSenderChatBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> messageModels;
    Context context;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    String recID;


    public MessageAdapter(ArrayList<MessageModel> arrayList, Context context, String recID) {
        this.messageModels = arrayList;
        this.context = context;
        this.recID = recID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender_chat, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver_chat, parent, false);
            return new ReceiverViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel message = messageModels.get(position);



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


            if (pos < 0 || context instanceof CommunityChatScreen){
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

            message.setFeeling(pos);

            String senderRoom = FirebaseAuth.getInstance().getUid() + recID;
            String receiverRoom = recID + FirebaseAuth.getInstance().getUid();

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child(message.getMessageId()).setValue(message);

            return true; // true is closing popup, false is requesting a new selection
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.getRoot())
                        .create();

                binding.everyone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        message.setMessage("This message has been removed.");
                        message.setFeeling(-1);
                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(FirebaseAuth.getInstance().getUid()+recID)
                                .child(message.getMessageId()).setValue(message);

                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(recID+FirebaseAuth.getInstance().getUid())
                                .child(message.getMessageId()).setValue(message);
                        dialog.dismiss();
                    }
                });

                binding.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("chats")
                                .child(FirebaseAuth.getInstance().getUid()+recID)
                                .child(message.getMessageId())
                                .setValue(null);
                        dialog.dismiss();
                    }
                });

                binding.cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

                return false;
            }
        });

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.senderChat.setText(message.getMessage());
            viewHolder.binding.senderTime.setText(formatTime(message.getTime()));


            if(message.getMessage().equals("photo")) {
                viewHolder.binding.senderTimeImg.setText(formatTime(message.getTime()));
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.senderTimeImg.setVisibility(View.VISIBLE);
                viewHolder.binding.senderTime.setVisibility(View.GONE);
                viewHolder.binding.senderChat.setVisibility(View.GONE);
                //viewHolder.binding.cons.setBackgroundResource(R.drawable.corners);
                Picasso.get().load(message.getPhoto()).placeholder(R.drawable.placeholder_img).into(viewHolder.binding.image);
            }



            if (message.getFeeling() >= 0) {
                int pos = message.getFeeling();
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
            viewHolder.binding.receiverChat.setText(message.getMessage());
            viewHolder.binding.receiverTime.setText(formatTime(message.getTime()));


            if(message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.receiverChat.setVisibility(View.GONE);
                Picasso.get().load(message.getPhoto()).placeholder(R.drawable.placeholder_img).into(viewHolder.binding.image);
            }


            if (message.getFeeling() >= 0) {
                int pos = message.getFeeling();
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

        SampleReceiverChatBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleReceiverChatBinding.bind(itemView);

        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        SampleSenderChatBinding binding;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SampleSenderChatBinding.bind(itemView);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }
}


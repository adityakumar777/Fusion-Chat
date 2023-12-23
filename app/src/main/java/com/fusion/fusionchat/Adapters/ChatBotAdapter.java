package com.fusion.fusionchat.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.SampleReceiverChatBinding;
import com.fusion.fusionchat.databinding.SampleSenderChatBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatBotAdapter extends RecyclerView.Adapter {

    ArrayList<String> list;
    Context context;
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;


    public ChatBotAdapter(Context context, ArrayList<String> list) {
        this.list = list;
        this.context = context;
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

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.binding.senderChat.setText(list.get(position));
            viewHolder.binding.senderChat.setBackgroundResource(R.drawable.bg_sender_chatbot);
            viewHolder.binding.feeling.setVisibility(View.INVISIBLE);
            viewHolder.binding.senderTime.setText(formatTime(System.currentTimeMillis()));

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.binding.receiverChat.setText(list.get(position));
            viewHolder.binding.receiverTime.setText(formatTime(System.currentTimeMillis()));

        }
        if (position == list.size() -1 ){
            YoYo.with(Techniques.FadeIn)
                    .duration(500)
                    .playOn(holder.itemView);
        }

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position%2==0)
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


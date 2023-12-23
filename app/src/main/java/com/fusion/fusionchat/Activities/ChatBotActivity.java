package com.fusion.fusionchat.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.fusion.fusionchat.Adapters.ChatBotAdapter;
import com.fusion.fusionchat.Models.MsgModel;
import com.fusion.fusionchat.Services.RetrofitAPI;
import com.fusion.fusionchat.databinding.ActivityChatBotBinding;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatBotActivity extends AppCompatActivity {

    ActivityChatBotBinding binding;
    ArrayList<String> list;
    ChatBotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();
        adapter = new ChatBotAdapter(this, list);
        binding.chatbot.setLayoutManager(new LinearLayoutManager(this));
        binding.chatbot.setAdapter(adapter);

        binding.backarrow.setOnClickListener(v -> onBackPressed());

        binding.button.setOnClickListener(v -> {

            String input = binding.editTextText.getText().toString();

            if (!input.isEmpty()){
            list.add(input);
            reply(input);
            binding.editTextText.setText("");
            adapter.notifyDataSetChanged();
            }
        });

        if (isNetworkAvailable())
        binding.presence.setText("Online");
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void reply(String question) {

        list.add("Typing...");
        String url = "http://api.brainshop.ai/get?bid=173152&key=kqt9LPzuyF0Su40q&uid=[uid]&msg=" + question;
        String Baseurl = "http://api.brainshop.ai/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Baseurl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {

                if (response.isSuccessful()) {
                    MsgModel msgModel = response.body();
                    String re=msgModel.getCnt().toString();
                    list.set(list.size()-1,re);
                    adapter.notifyDataSetChanged();
                    binding.chatbot.smoothScrollToPosition(list.size());
                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {

                }
        });

        /*String response;
        int randomIndex;
        Random random = new Random();
        String input = question.toLowerCase();

        List<String> hiResponses = new ArrayList<>();
        hiResponses.add("Hello!");
        hiResponses.add("Hi there!");
        hiResponses.add("Hey, what's up?");

        List<String> howAreYouResponses = new ArrayList<>();
        howAreYouResponses.add("I'm just a computer program, so i always feel good!");
        howAreYouResponses.add("I'm good. How can I assist you?");
        howAreYouResponses.add("I don't have feelings, but I'm ready to assist you.");

        if (input.contains("hi") || input.contains("hello")) {
            randomIndex = random.nextInt(hiResponses.size());
            response = hiResponses.get(randomIndex);
        } else if (input.contains("your name")) {
            response = "I don't have a name. You can call me Chatbot.";
        } else if (input.contains("weather")) {
            response = "I'm sorry, I don't have access to real-time weather data.";
        } else if (input.contains("tell me a joke")) {
            response = "Sure, here's one: Why don't scientists trust atoms? Because they make up everything!";
        } else if (input.contains("how are you")) {
            randomIndex = random.nextInt(howAreYouResponses.size());
            response = howAreYouResponses.get(randomIndex);
        } else if (input.contains("bye")) {
            response = "Goodbye! Feel free to come back anytime.";
        } else {
            response = "I'm not sure how to respond to that. Ask me something else.";
        }*/
        //return response;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(ChatBotActivity.this,MainActivity.class);
        startActivity(i);
        finish();
    }
}
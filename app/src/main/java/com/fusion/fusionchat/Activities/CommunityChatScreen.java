package com.fusion.fusionchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fusion.fusionchat.Adapters.CommunityMessageAdapter;
import com.fusion.fusionchat.Models.MessageModel;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityCommunityChatScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;


public class CommunityChatScreen extends AppCompatActivity {
    private ActivityCommunityChatScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        String communityPicResId = getIntent().getStringExtra("communityPicResId");
        String communityName = getIntent().getStringExtra("communityName");
        String creatorUid = getIntent().getStringExtra("creatorUid");
        String communityId = getIntent().getStringExtra("communityId");



        binding.communityName.setText(communityName);
        Picasso.get().load(communityPicResId).placeholder(R.drawable.default_profile_pic).into(binding.communityPic);

      FirebaseDatabase database = FirebaseDatabase.getInstance();
      ArrayList<MessageModel> list = new ArrayList<>();
      String  senderUid = FirebaseAuth.getInstance().getUid();
      CommunityMessageAdapter adapter = new CommunityMessageAdapter(list,this,creatorUid,communityId);
     binding.chatRecyclerview.setLayoutManager(new LinearLayoutManager(this));
     binding.chatRecyclerview.setAdapter(adapter);


     binding.communityTop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Intent intent = new Intent(CommunityChatScreen.this, CommunityDetailsActivity.class);
             intent.putExtra("creatorUid",creatorUid);
             intent.putExtra("communityId",communityId);
             intent.putExtra("communityName",communityName);
             intent.putExtra("communityPicResId",communityPicResId);
             startActivity(intent);
             overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
         }
     });

        database.getReference()
                .child("Community")
                .child(creatorUid)
                .child(communityId)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                            list.add(messageModel);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CommunityChatScreen.this, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

     binding.send.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            String message =  binding.messagebox.getText().toString();

            if (!(message.isEmpty())){

            MessageModel model = new MessageModel(senderUid,message,System.currentTimeMillis());
             String randomKey = database.getReference().push().getKey();
             model.setMessageId(randomKey);
            binding.messagebox.setText("");
            database.getReference().child("Community")
                    .child(creatorUid)
                    .child(communityId)
                    .child("messages")
                    .child(randomKey)
                    .setValue(model);
            }

         }
     });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CommunityChatScreen.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}
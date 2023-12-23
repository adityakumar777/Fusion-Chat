package com.fusion.fusionchat.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fusion.fusionchat.Adapters.MessageAdapter;
import com.fusion.fusionchat.Models.MessageModel;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityChatScreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatScreenActivity extends AppCompatActivity {
    private ActivityChatScreenBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
        String senderUid;

    String profilePicResId;
    String token;
    String username;
    String receiverUid;
        private Calendar calendar;
    private ActivityResultLauncher<String> mTakePic;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar2);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        calendar = Calendar.getInstance();

        senderUid = firebaseAuth.getUid();
        receiverUid = getIntent().getStringExtra("userID");

        //Getting the information of receiver from UserAdapter to toolbar

        profilePicResId = getIntent().getStringExtra("profilePicResId");
        username = getIntent().getStringExtra("username");
        token = getIntent().getStringExtra("token");

        //Set the profilePic and username of user on profile
        binding.usernameOnChatScreen.setText(username);
        Picasso.get().load(profilePicResId).placeholder(R.drawable.default_profile_pic).into(binding.profilePic);


        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 2);

            }
        });


        final Handler handler = new Handler();
        binding.messagebox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String status = snapshot.getValue(String.class);
                    if(!status.isEmpty()) {
                        if(status.equals("Offline")) {
                            binding.presence.setVisibility(View.GONE);
                        } else {
                            binding.presence.setText(status);
                            binding.presence.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        final MessageAdapter messageAdapter = new MessageAdapter(messageModels,this,receiverUid);
        binding.chatRecyclerview.setAdapter(messageAdapter);
        binding.chatRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        //Standard logic to know who sent message to whom
        final String senderRoom = senderUid + receiverUid;
        final String receiverRoom = receiverUid + senderUid;




        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTakePic.launch("image/*");
            }
        });

        //Logic to upload the image on FirebaseStorage , It may take some time
        mTakePic = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {

                StorageReference reference = storage.getReference().child("chats")
                        .child(System.currentTimeMillis() + "");
                reference.putFile(result).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String filePath = uri.toString();

                                    String messageTxt = binding.messagebox.getText().toString();
                                    ;
                                    MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(),messageTxt,calendar.getTimeInMillis());
                                    model.setMessage("photo");
                                    model.setPhoto(filePath);
                                    binding.messagebox.setText("");

                                    String randomKey = database.getReference().push().getKey();

                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child(randomKey)
                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    database.getReference().child("chats")
                                                            .child(receiverRoom)
                                                            .child(randomKey)
                                                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                }
                                                            });
                                                }
                                            });

                                    //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        database.getReference().child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MessageModel messageModel = dataSnapshot.getValue(MessageModel.class);
                            messageModel.setMessageId(dataSnapshot.getKey());
                            messageModels.add(messageModel);
                        }
                        //Changes the recyclerView items in runtime
                        messageAdapter.notifyDataSetChanged();
                        binding.chatRecyclerview.smoothScrollToPosition(messageModels.size());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                   //     Toast.makeText(ChatScreenActivity.this, error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfilePictureDialog(profilePicResId,username);
            }
        });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTheMessage(senderUid,senderRoom,receiverRoom);
            }
        });
    }

    private void sendTheMessage(String senderUid , String senderRoom , String receiverRoom) {
        String message = binding.messagebox.getText().toString();
        long currentTime = System.currentTimeMillis();
         MessageModel model = new MessageModel(senderUid,message,currentTime);
        binding.messagebox.setText("");

        String randomKey = database.getReference().push().getKey();
        database.getReference().child("chats")
                .child(senderRoom)
                .child(randomKey)
                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("chats")
                                .child(receiverRoom)
                                .child(randomKey)
                                .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        sendNotification(username, model.getMessage(),token);

                                    }
                                });
                    }
                });
    }


    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData
                    , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "key=AAAAodiCZ7Q:APA91bHKf8Wcdpkz9u7zPvHCMlb2Y4MbQb_zBYt3xzIR3m1eKNl7ADwMh49rJ-RVsIliCbFry75WwwXhaVTO57YL3QaxVLYVuDBRHMN05iECiYbXOSEZj451JWd44ZMdBkWNN9jE5yms";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization", key);

                    return map;
                }
            };

            queue.add(request);


        } catch (Exception ex) {

        }


    }


    public void showProfilePictureDialog(String profilePicResId ,String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.pop_profile_pic, null);

        ImageView enlargedImageView = dialogView.findViewById(R.id.popUpProfilePic);
        TextView enlargedTextView = dialogView.findViewById(R.id.popUpName);

        enlargedTextView.setText(username);
        Picasso.get().load(profilePicResId).resize(200,250).placeholder(R.drawable.default_profile_pic).into(enlargedImageView);

        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Changing the alert dialog box size according to me
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = 700; // Set the width of the dialog
         // Set the height of the dialog
        alertDialog.getWindow().setAttributes(layoutParams);

        enlargedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the dialog from screen when someone tap on image again
                alertDialog.dismiss();
            }
        });
    }

   @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChatScreenActivity.this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri result = data.getData();
            StorageReference reference = storage.getReference().child("chats")
                    .child(System.currentTimeMillis() + "");
            reference.putFile(result).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String filePath = uri.toString();

                                String messageTxt = binding.messagebox.getText().toString();
                                ;
                                MessageModel model = new MessageModel(FirebaseAuth.getInstance().getUid(),messageTxt,calendar.getTimeInMillis());
                                model.setMessage("photo");
                                model.setPhoto(filePath);
                                binding.messagebox.setText("");

                                String randomKey = database.getReference().push().getKey();

                                database.getReference().child("chats")
                                        .child(FirebaseAuth.getInstance().getUid()+receiverUid)
                                        .child(randomKey)
                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                database.getReference().child("chats")
                                                        .child(receiverUid+FirebaseAuth.getInstance().getUid())
                                                        .child(randomKey)
                                                        .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                            }
                                                        });
                                            }
                                        });

                                //Toast.makeText(ChatActivity.this, filePath, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });




}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id==R.id.block){
            Toast.makeText(this, "User is getting blick....", Toast.LENGTH_SHORT).show();
        }
        else {
            showProfilePictureDialog(profilePicResId,username);
        }

        return super.onOptionsItemSelected(item);

    }
}
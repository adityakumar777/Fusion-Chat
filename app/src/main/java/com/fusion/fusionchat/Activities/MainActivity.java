package com.fusion.fusionchat.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.viewpager2.widget.ViewPager2;
import com.fusion.fusionchat.Adapters.FragmentsAdapter;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ImageCapture imageCapture;
    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);


        //This line is to initialize the app with current user so that if he opens it again he will be redirected to MainActivity
        FirebaseApp.initializeApp(this);


        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token", token);
                        FirebaseDatabase.getInstance().getReference()
                                .child("Users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                    }
                });



        FragmentsAdapter fragmentsAdapter = new FragmentsAdapter(MainActivity.this);
        binding.viewPager.setAdapter(fragmentsAdapter);

        binding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (binding.tablayout.getSelectedTabPosition() != position) {
                    binding.tablayout.getTabAt(position).select();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //items available in menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.camera){

        }


        else if (id == R.id.share) {
            String appLink = "https://drive.google.com/file/d/1j5RapGyhejfgB431rTOUo8mts1IkWyD0/view?usp=drive_link";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Link to download Fusion Chat " + appLink);
            startActivity(Intent.createChooser(shareIntent, "Share App Link"));
        }
        else if (id == R.id.tandc) {
            Intent intent = new Intent(MainActivity.this, TermsAndConditions.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        else if (id == R.id.contactUs) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // This MIME type is used for emails
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"as905691@gmail.com"});
            intent.putExtra(Intent.EXTRA_TEXT, "Your problem or issue here!");
            startActivity(Intent.createChooser(intent, "Send Email"));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }

        else if (id == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {

        }
    }
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
        } else {
            this.doubleBackToExitPressedOnce = true;
            FancyToast.makeText(getApplicationContext(), "Press back again!", FancyToast.LENGTH_SHORT, FancyToast.INFO, true).show();
            new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                  doubleBackToExitPressedOnce = false;
              }
          },2000);
        }
    }

}
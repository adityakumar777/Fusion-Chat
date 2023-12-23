package com.fusion.fusionchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fusion.fusionchat.databinding.ActivitySplashBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        YoYo.with(Techniques.BounceInLeft)
                .duration(700)
                .playOn(binding.splashLogo);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nevigateToActivity();
            }
        },700);
    }
    public void nevigateToActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // User is already authenticated, navigate to chat activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User not authenticated, show signup activity
            startActivity(new Intent(this, WalkthroughActivity.class));
        }
        finish();
    }
}
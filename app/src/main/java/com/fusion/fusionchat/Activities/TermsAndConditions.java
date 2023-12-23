package com.fusion.fusionchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.fusion.fusionchat.databinding.ActivityTermsAndConditionsBinding;

public class TermsAndConditions extends AppCompatActivity {

    ActivityTermsAndConditionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TermsAndConditions.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
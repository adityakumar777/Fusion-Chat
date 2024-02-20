package com.fusion.fusionchat.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityTermsAndConditionsBinding;

public class TermsAndConditions extends AppCompatActivity {

    ActivityTermsAndConditionsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       binding = ActivityTermsAndConditionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


       String formattedText = getString(R.string.terms_and_conditions);

        // Use Html.fromHtml() for versions below Nougat (API 24)
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            binding.terms.setText(Html.fromHtml(formattedText));
        } else {
            // Use HtmlCompat.fromHtml() for Nougat (API 24) and above
            binding.terms.setText(HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY));
        }

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
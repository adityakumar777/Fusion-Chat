package com.fusion.fusionchat.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.fusion.fusionchat.Adapters.WalkthroughAdapter;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityWalkthroughBinding;

public class WalkthroughActivity extends AppCompatActivity {

    private ActivityWalkthroughBinding binding;
    private TextView[] dots;

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            setUpIndicator(position);
            if (position > 0) {
                binding.backBtn.setVisibility(View.VISIBLE);
            } else {
                binding.backBtn.setVisibility(View.INVISIBLE);
            }
            if (position == 2) {
                binding.nextBtn.setText("Finish");
            } else {
                binding.nextBtn.setText("Next");
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalkthroughBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nevigateToSignUpActivity();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem(0) > 0)
                    binding.sliderViewpager.setCurrentItem(getItem(-1), true);
            }
        });
        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem(0) < 2)
                    binding.sliderViewpager.setCurrentItem(getItem(+1), true);
                else {
                    nevigateToSignUpActivity();
                }
            }
        });

        WalkthroughAdapter walkthroughAdapter = new WalkthroughAdapter(WalkthroughActivity.this);
        binding.sliderViewpager.setAdapter(walkthroughAdapter);
        setUpIndicator(0);
        binding.sliderViewpager.addOnPageChangeListener(viewListener);
    }
    private void nevigateToSignUpActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void setUpIndicator(int position) {

        dots = new TextView[3];
        binding.dotIndicator.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY));
            /*
                "&#8226": This is an HTML entity reference, &#8226, which represents a bullet point (•) character.
    Html.FROM_HTML_MODE_LEGACY: This is a flag that specifies how to interpret the HTML content.
    In this case, FROM_HTML_MODE_LEGACY is used, which indicates that you want to use the legacy HTML rendering mode.

             */
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.grey, getApplicationContext().getTheme()));
            binding.dotIndicator.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.black, getApplicationContext().getTheme()));

    }


    private int getItem(int i) {
        return binding.sliderViewpager.getCurrentItem() + i;
    }
}

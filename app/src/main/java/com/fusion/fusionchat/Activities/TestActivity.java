package com.fusion.fusionchat.Activities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fusion.fusionchat.Adapters.ChatBotAdapter;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityTestBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TestActivity extends AppCompatActivity {
    ActivityTestBinding binding;
    private EditText editText;
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

                editText = findViewById(R.id.editText);
                button = findViewById(R.id.button);
                textView = findViewById(R.id.textView);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userInput = editText.getText().toString();
                        if (!userInput.isEmpty()) {
                            animateTyping(userInput);
                        }
                    }
                });
            }

    private void animateTyping(String text) {
        char[] charArray = text.toCharArray();
        long interval = 100L; // milliseconds
        Handler handler = new Handler(Looper.getMainLooper());

        textView.setText("");

        for (int i = 0; i < charArray.length; i++) {
            final int index = i;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String currentText = textView.getText().toString();
                    textView.setText(currentText + charArray[index]);

                    // Scroll to the bottom
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.getParent().requestChildFocus(textView, textView);
                        }
                    });
                }
            }, i * interval);
        }
    }

}

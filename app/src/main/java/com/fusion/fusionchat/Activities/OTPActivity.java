package com.fusion.fusionchat.Activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.fusion.fusionchat.R;
import com.fusion.fusionchat.databinding.ActivityOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private ActivityOtpBinding binding;
    private String number;
    private String otpOfFirebase;

    private FirebaseAuth mAuth;
    private String formattedNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();


        showToastOfOTPSent();
        otpTextInput(); // Here is code to move control to next box

        number = getIntent().getStringExtra("number");
        formattedNumber = getIntent().getStringExtra("formattedNumber");
        otpOfFirebase = getIntent().getStringExtra("verificationId");
        binding.number.setText(formattedNumber);

        binding.resendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendOTP();
            }
        });

        binding.verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.box1.getText().toString().isEmpty()
                        || binding.box2.getText().toString().isEmpty()
                        || binding.box3.getText().toString().isEmpty()
                        || binding.box4.getText().toString().isEmpty()
                        || binding.box5.getText().toString().isEmpty()
                        || binding.box6.getText().toString().isEmpty()
                ) {
                    FancyToast.makeText(getApplicationContext(), "OTP is not full!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                } else {
                    if (otpOfFirebase != null) {
                        // Hide button show loading
                        binding.verifyBtn.setVisibility(View.INVISIBLE);
                        binding.signInLoading.setVisibility(View.VISIBLE);

                        String userEnteredOtp =
                                binding.box1.getText().toString().trim() +
                                        binding.box2.getText().toString().trim() +
                                        binding.box3.getText().toString().trim() +
                                        binding.box4.getText().toString().trim() +
                                        binding.box5.getText().toString().trim() +
                                        binding.box6.getText().toString().trim();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpOfFirebase, userEnteredOtp);
                        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                binding.signInLoading.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), UserDataActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("number",number);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                }
                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                FancyToast.makeText(getApplicationContext(), "Invalid OTP", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                                binding.verifyBtn.setVisibility(View.VISIBLE);
                                binding.signInLoading.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }
            }
        });


    }

    private void sendOTP() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Hide loading show button
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                FancyToast.makeText(getApplicationContext(), "Verification Failed!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                FancyToast.makeText(getApplicationContext(), "Code Sent!", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber(formattedNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void showToastOfOTPSent() {
        Toast toast = new Toast(this);
        View view = getLayoutInflater().inflate(R.layout.otp_sent_toast_layout,null);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL,0,100);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(params);
        toast.show();

    }

    //This method moves the cursor to the next edit text without click on it
    private void otpTextInput() {
        binding.box1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.box2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)
                    binding.box1.requestFocus();
            }
        });
        binding.box2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                binding.box3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)

                    binding.box2.requestFocus();
            }
        });
        binding.box3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.box4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length() == 0)
                    binding.box3.requestFocus();
            }
        });
        binding.box4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.box5.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)
                    binding.box4.requestFocus();
            }
        });
        binding.box5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.box6.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)
                    binding.box5.requestFocus();
            }
        });
        binding.box6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (binding.box6.getText().toString().length()==1)
                   binding.box6.setClickable(false);

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==1){
                hideKeyboard(binding.box6);
                }
            }
        });
    }
        private void hideKeyboard(EditText editText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
}
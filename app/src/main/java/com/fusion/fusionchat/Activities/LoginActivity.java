    package com.fusion.fusionchat.Activities;


    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.os.Bundle;
    import android.text.Editable;
    import android.text.TextWatcher;
    import android.view.View;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.EditText;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import com.daimajia.androidanimations.library.Techniques;
    import com.daimajia.androidanimations.library.YoYo;
    import com.fusion.fusionchat.databinding.ActivityLoginBinding;
    import com.google.firebase.FirebaseException;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.PhoneAuthCredential;
    import com.google.firebase.auth.PhoneAuthOptions;
    import com.google.firebase.auth.PhoneAuthProvider;
    import com.shashank.sony.fancytoastlib.FancyToast;
    import java.util.concurrent.TimeUnit;

    public class LoginActivity extends AppCompatActivity {

        private static final int PERMISSION_REQUEST_CODE = 123;
        private String permissions[] = {
                                        "android.permission.READ_EXTERNAL_STORAGE",
                                         "android.permission.WRITE_EXTERNAL_STORAGE"   };
        private ActivityLoginBinding binding;
        private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            requestPermissions(permissions, PERMISSION_REQUEST_CODE);

            binding.cpicker.registerCarrierNumberEditText(binding.number);

           numberTextWatcher();

            binding.goBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (binding.number.getText().toString().trim().isEmpty()) {
                        binding.number.setError("Enter Number");
                        YoYo.with(Techniques.Shake) //Shaking Animation
                                .duration(700)
                                .playOn(binding.goBtn);
                    } else if (binding.number.getText().toString().trim().length() != 10) {
                        binding.number.setError("Wrong Number");
                         YoYo.with(Techniques.Shake)
                                .duration(700)
                                .playOn(binding.goBtn);
                    } else {
                        sendOTP();
                    }
                }
            });
        }

        private void numberTextWatcher() {
            binding.number.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (binding.number.getText().toString().length()==10)
                        hideKeyboard(binding.number);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }

        private void hideKeyboard(EditText editText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }

        public void sendOTP() {
            // Hide button show loading
            binding.goBtn.setVisibility(View.INVISIBLE);
            binding.loading.setVisibility(View.VISIBLE);

            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // Hide loading show button
                    binding.goBtn.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    binding.goBtn.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.INVISIBLE);
                    FancyToast.makeText(getApplicationContext(), "Verification Failed!", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    FancyToast.makeText(getApplicationContext(), "Code Sent!", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                    Intent intent = new Intent(getApplicationContext(), OTPActivity.class);
                    intent.putExtra("number", binding.cpicker.getFullNumber());
                    intent.putExtra("formattedNumber", binding.cpicker.getFormattedFullNumber());
                    intent.putExtra("verificationId", verificationId);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            };



            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber(binding.cpicker.getFormattedFullNumber())       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // (optional) Activity for callback binding
                            // If no activity is passed, reCAPTCHA verification can not be used.
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode > 0 && requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission are granted here!
                }
                else {
                    //Permissions are not granted so user will be redirected tp request them again
                    requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                }
            }
        }
    }

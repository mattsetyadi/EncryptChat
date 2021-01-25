package com.setyadi.encryptchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button SendVerificationCodeButton, VerifyButton;
    private EditText InputPhoneNumber, InputVerificationCode;

    //deklarasi phone verif
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerificationCodeButton = (Button) findViewById(R.id.send_ver_code_button);
        VerifyButton = (Button) findViewById(R.id.verify_button);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_nnumber_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        //perintah ketika tombol send verification button di click untuk mengirim code verification
        SendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = InputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    //Toast the user like TELL THE USER
                    Toast.makeText(PhoneLoginActivity.this, "Please enter your phone number first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("please wait, while we are authenticating your phone...");
                    //ketika user menekan layar diluar dialog tidak akan membatalkan proses karena "FALSE"
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    //console dari firebase untuk phone verification
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks dari deklarasi di atas
                }
            }
        });



        //Fungsi VERIFY BUTTON
        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("please wait, while we are verifying verification code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();


                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });


        //membuat kondisi ketika callback berhasil atau gagal
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please enter correct phone number with your country code...", Toast.LENGTH_SHORT).show();


                //jika user gagal verifikasi maka edit text pmasukan nomer hp dan send verification code akan muncul kembali
                SendVerificationCodeButton.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                //dan edit text masukan verifikasi code serta button verify akan kembali manghilang jika dalam kasus ini
                VerifyButton.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            //jika kode sudah dikirimkan, app harus memberitahu user
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token)
            {
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent, please check and verify...", Toast.LENGTH_SHORT).show();

                //perintah ini sekaligus membuat tombol send verification code dan input phone number menjadi invisible
                SendVerificationCodeButton.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                //sekaligus membuat edit text input verification code dan button Verify menjadi VISIBLE
                VerifyButton.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);

            }
        };




    }
    //di dapat dari docs FIREBASE untuk sign in ke user
    //jika semua sudah memenuhi syarat, maka code ini akan mengirim user ke main activity.xml
    //masuk menggunakan credential dan penggunaan loading bar
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : "  +  message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}


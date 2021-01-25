package com.setyadi.encryptchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class RegisterActivity extends AppCompatActivity
{
    private Button CreateAccountButton;
    private EditText UserEmail, UserPassword;
    private TextView AlreadyHaveAccountLink;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
             CreateNewAccount();
            }
        });


    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                         if(task.isSuccessful())
                         {
                             final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                             FirebaseInstanceId.getInstance().getInstanceId()
                                     .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                         @Override
                                         public void onSuccess(InstanceIdResult instanceIdResult)
                                         {
                                             String deviceToken = instanceIdResult.getToken();
                                             RootRef.child(currentUserId).child("device_token")
                                                     .setValue(deviceToken)
                                                     .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                         @Override
                                                         public void onComplete(@NonNull Task<Void> task)
                                                         {
                                                             if(task.isSuccessful())
                                                             {
                                                                 SendUserToMainActivity();
                                                                 Toast.makeText(RegisterActivity.this, "Account created successfully...", Toast.LENGTH_SHORT).show();
                                                                 loadingBar.dismiss();
                                                             }
                                                         }
                                                     });
                                         }
                                     });



                         }
                         else
                         {
                             String message = task.getException().toString();
                             Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                             loadingBar.dismiss();
                         }
                        }
                    });
        }
    }

    private void InitializeFields()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_account_link);

        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);

    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}

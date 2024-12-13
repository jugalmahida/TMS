package com.example.tms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tms.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class ForgetPassword extends AppCompatActivity {

    private ActivityForgetPasswordBinding binding;
    private String email;
    private final DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference().child("Teachers");

    private static ArrayList<String> allEmails = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();
        binding.txtFgobackt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForgetPassword.this, SigninActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = binding.edFEmail.getText().toString().trim().toLowerCase();
                if(!email.isBlank()){
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                String key = dataSnapshot.getKey();
                                if(!(key.equals("Announcements")) && !(key.equals("Materials"))) {
                                    allEmails.add("" + dataSnapshot.child("email").getValue());
//                                    Log.i("All Emails",""+dataSnapshot.child("email").getValue().toString());
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
//                    Log.i("All Emails",""+allEmails);
                    if(allEmails.contains(email)){
                        binding.tilFEmail.setError("");
                        EditText txtEmail;
                        Button btnEditEmail;
                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                        View view1 = inflater.inflate(R.layout.alert_dialog, null);
                        AlertDialog builder = new AlertDialog.Builder(ForgetPassword.this).create();
                        builder.setCancelable(true);
                        builder.setView(view1);
                        builder.setTitle("Email Verification");
                        builder.setMessage("A Email has been send on below email \nPlease Click on the link in your email for new password \nAfter goto login with new password");
                        txtEmail = view1.findViewById(R.id.edemailforalert);
                        btnEditEmail = view1.findViewById(R.id.btneditemail);
                        txtEmail.setEnabled(false);
                        txtEmail.setText(email);
                        builder.show();
                        btnEditEmail.setVisibility(View.GONE);

                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Email is send password reset email", Toast.LENGTH_SHORT).show();
//                                            Intent intent = new Intent(ForgetPassword.this, SigninActivity.class);
//                                            startActivity(intent);
//                                            finish();
                                        } else {
                                            // Password reset email failed to send
                                            Toast.makeText(getApplicationContext(), "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }else{
                        binding.tilFEmail.setError("Email not Found");
                    }
                }else{
                    binding.tilFEmail.setError("Please Enter Email");
                }
            }
        });
    }
}
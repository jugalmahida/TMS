package com.example.tms;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;

import android.view.View;

import android.widget.Toast;
import com.example.tms.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SigninActivity extends AppCompatActivity {
    private String email,password,key;
    private ActivitySigninBinding binding;
    private FirebaseAuth mAuth;
    boolean isAllFilled = false;
    private DatabaseReference databaseReference;
    private TeacherModel currentUser = new TeacherModel();
    private DatabaseReference tcnameReference=FirebaseDatabase.getInstance().getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Teachers");

        binding.txtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SigninActivity.this, ForgetPassword.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
    public void toSignup(View v){
        Intent intent = new Intent(SigninActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }
    public void Signin(View v){
        email = binding.edEmail.getText().toString().trim().toLowerCase();
        password = binding.edPassword.getText().toString().trim();
        if(email.isBlank() && password.isBlank()){
            binding.tilLEmail.setError("Email is Required");
            binding.tilLPassword.setError("Password is Required");
            isAllFilled = false;
        } else {
            binding.tilLEmail.setError("");
            binding.tilLPassword.setError("");
            isAllFilled = CheckAllFields();
            if(isAllFilled) {
                binding.progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Get Data from Firebase of Current User
                                    tcnameReference.child("Teachers").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                key = childSnapshot.getKey();
                                                Log.i("Pushed Key", key);
                                                currentUser.setTcName((String) childSnapshot.child("tcName").getValue());
                                                currentUser.setEmail((String) childSnapshot.child("email").getValue());
                                                currentUser.setFullName((String) childSnapshot.child("fullName").getValue());
                                                currentUser.setPhone((String) childSnapshot.child("phone").getValue());
                                                currentUser.setStandards((ArrayList<String>) childSnapshot.child("standards").getValue());
                                                currentUser.setSubjects((ArrayList<String>) childSnapshot.child("subjects").getValue());
                                            }
                                            SharedPreferences sharedPreferences = getSharedPreferences("SystemPre", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putBoolean("isLogin", true);
                                            editor.putString("email", email);
                                            editor.putString("key", key);
                                            editor.putString("tcName", currentUser.getTcName());
                                            editor.putString("name", currentUser.getFullName());
                                            editor.putString("phone", currentUser.getPhone());
                                            editor.putLong("lastLogin",System.currentTimeMillis());
                                            editor.commit();
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                currentUser.reload();
                                            }
                                            binding.progressBar.setVisibility(View.GONE);
                                            Intent intent = new Intent(SigninActivity.this, DashboardActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SigninActivity.this, "Invalid Email or Password.", Toast.LENGTH_SHORT).show();
                                    binding.progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        }
    }
    private boolean CheckAllFields() {
        if(binding.edEmail.getText().toString().isBlank()){
            binding.tilLEmail.setError("Email is Required");
            return false;
        }if(binding.edPassword.getText().toString().isBlank()){
            binding.tilLPassword.setError("Password is Required");
            return false;
        }
        binding.tilLEmail.setError("");
        binding.tilLPassword.setError("");
        return true;
    }
}
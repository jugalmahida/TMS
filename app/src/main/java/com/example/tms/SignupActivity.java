package com.example.tms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tms.databinding.ActivitySignupBinding;
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
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private String tname,name,email,password,phone;
    private boolean isRestcName,isRestPhone;
    private DatabaseReference tcNameDataRefre = FirebaseDatabase.getInstance().getReference("Teachers");
    private ActivitySignupBinding binging;
    List<Integer> stdlist = new ArrayList<>();
    ArrayList<Integer> sublist = new ArrayList<>();
    boolean[] selectedStd;
    boolean[] selectedSub;
    String[] stdArray = {"1","2","3","4","5","6","7","8","9","10"};
    String[] subArray = {"Gujarati", "English", "Maths", "Science", "Social Science", "Hindi", "Environment","Sanskrit","Computer"};
    String finalSelectedStd = "";
    String finalSelectedSub = "";
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private boolean isAllFileds = false;
    TextView txtEmail;
    Button btnEditEmail;
    private SharedPreferences sharedPreferences;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binging = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binging.getRoot());
        getSupportActionBar().hide();
        selectedStd = new boolean[stdArray.length];
        selectedSub = new boolean[subArray.length];
        //Get Firebase Instance
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Teachers");
        // Show Preview of Std and Sub
        binging.btnPreviewSubStd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finalSelectedStd.isEmpty()||finalSelectedSub.isEmpty()){
                    Toast.makeText(SignupActivity.this, "Please Select Subjects And Standard", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog builder = new AlertDialog.Builder(SignupActivity.this).create();
                    builder.setTitle("Your Details");
                    builder.setMessage("Your Subjects :- \n" + finalSelectedSub + "\n\n" + "Your Standards :- \n" + finalSelectedStd);
                    builder.setButton(Dialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }
            }
        });
        // Show Dialog of Std
        binging.sStd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setTitle("Select Standard");
                // set dialog non cancelable
                builder.setCancelable(false);
                builder.setMultiChoiceItems(stdArray, selectedStd, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            stdlist.add(i);
                            Collections.sort(stdlist);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            stdlist.remove(Integer.valueOf(i));
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < stdlist.size(); j++) {
                            // concat array value
                            stringBuilder.append(stdArray[stdlist.get(j)]);
                            // check condition
                            if (j != stdlist.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        binging.sStd.setText(stringBuilder.toString());
                        finalSelectedStd = stringBuilder.toString();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedStd.length; j++) {
                            // remove all selection
                            selectedStd[j] = false;
                            // clear language list
                            stdlist.clear();
                            // clear text view value
                            binging.sStd.setText("");
                            finalSelectedStd = "";
                        }

                    }
                });
                // show dialog
                builder.show();
            }
        });
        // Show Dialog of Subjects
        binging.sSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                // set title
                builder.setTitle("Select Subjects");

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(subArray, selectedSub, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            sublist.add(i);
                            // Sort array list
                            Collections.sort(sublist);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            sublist.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < sublist.size(); j++) {
                            // concat array value
                            stringBuilder.append(subArray[sublist.get(j)]);

                            // check condition
                            if (j != sublist.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        binging.sSub.setText(stringBuilder.toString());
                        finalSelectedSub = stringBuilder.toString();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedSub.length; j++) {
                            // remove all selection
                            selectedSub[j] = false;
                            // clear language list
                            sublist.clear();
                            // clear text view value
                            binging.sSub.setText("");
                            finalSelectedSub = "";
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
    }
    public void toSignin(View v){
        Intent intent = new Intent(SignupActivity.this, SigninActivity.class);
        startActivity(intent);
        finish();
    }

    public void Signup(View v){
        tname = binging.edClassesName.getText().toString().trim();
        name = binging.edName.getText().toString().trim();
        email = binging.edEmail.getText().toString().trim().toLowerCase();
        password = binging.edPassword.getText().toString().trim();
        phone = binging.edPhone.getText().toString().trim();
        if(email.isBlank() && password.isBlank() && name.isBlank() && tname.isBlank() &&  finalSelectedSub.isBlank() &&  finalSelectedStd.isBlank() && phone.isBlank() ){
            binging.tilSEmail.setError("Email is Required");
            binging.tilSPassword.setError("Password is Required");
            binging.tilSFullName.setError("Full Name is Required");
            binging.tilSTcName.setError("Tuition Classes Name is Required");
            binging.tilSPhoneNumber.setError("Phone Number is Required");
            Toast.makeText(this, "Please Select Subject And Standard", Toast.LENGTH_SHORT).show();
            isAllFileds = false;
        } else {
            binging.tilSTcName.setError("");
            binging.tilSFullName.setError("");
            binging.tilSEmail.setError("");
            binging.tilSPhoneNumber.setError("");
            binging.tilSPassword.setError("");
            isAllFileds = CheckAllFields();
            if(isAllFileds){
                if(isValidEmail(email)){
                if(password.length() >= 6) {
                    if (phone.length() == 10) {
                        if(isValidPhoneNumber(phone)){
                            binging.progressBar2.setVisibility(View.VISIBLE);
                        String[] finalstds = finalSelectedStd.split(", ");
                        ArrayList<String> stdlist = new ArrayList<String>(Arrays.asList(finalstds));
                        String[] finalsubs = finalSelectedSub.split(", ");
                        ArrayList<String> sublist = new ArrayList<String>(Arrays.asList(finalsubs));
                        TeacherModel tm = new TeacherModel(tname, name, stdlist, sublist, email, phone);
                        // Is registered
                        ArrayList<String> tcNames = new ArrayList<>();
                        ArrayList<String> phoneNumbers = new ArrayList<>();
                        tcNameDataRefre.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String tcname12;
                                String phoneNumber;
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    tcname12 = (String) dataSnapshot.child("tcName").getValue();
                                    phoneNumber = (String) dataSnapshot.child("phone").getValue();
                                    tcNames.add(tcname12);
                                    phoneNumbers.add(phoneNumber);
                                    phoneNumbers.removeAll(Collections.singletonList(null));
                                    tcNames.removeAll(Collections.singletonList(null));
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    isRestcName = tcNames.stream().anyMatch(tname::equalsIgnoreCase);
                                    isRestPhone = phoneNumbers.stream().anyMatch(phone::equalsIgnoreCase);
                                }
                                if(!isRestcName){
                                    if(!isRestPhone) {
                                        mAuth.createUserWithEmailAndPassword(email, password)
                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                                                        View view = inflater.inflate(R.layout.alert_dialog, null);
                                                        AlertDialog builder = new AlertDialog.Builder(SignupActivity.this).create();
                                                        builder.setCancelable(false);
                                                        builder.setView(view);
                                                        builder.setTitle("Email Verification");
                                                        builder.setMessage("A Email has been send on below email \nYou will be redirected to Dashboard in few moments...\nNot You? Type New Email to Resend\n");
                                                        txtEmail = view.findViewById(R.id.edemailforalert);
                                                        btnEditEmail = view.findViewById(R.id.btneditemail);
                                                        txtEmail.setEnabled(false);
                                                        txtEmail.setText(email);
                                                        btnEditEmail.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                binging.edEmail.requestFocus();
                                                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                imm.showSoftInput(binging.edEmail, InputMethodManager.SHOW_IMPLICIT);
                                                                builder.dismiss();
                                                            }
                                                        });
                                                        builder.setButton(Dialog.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                builder.dismiss();
                                                            }
                                                        });
                                                        builder.show();
                                                        if (task.isSuccessful()) {
                                                            mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Timer timer = new Timer();
                                                                        timer.schedule(new TimerTask() {
                                                                            @Override
                                                                            public void run() {
                                                                                mAuth.getCurrentUser().reload();
                                                                                if (mAuth.getCurrentUser().isEmailVerified()) {
                                                                                    timer.cancel();
                                                                                    builder.dismiss();
                                                                                    String ukey = databaseReference.push().getKey();
                                                                                    databaseReference.child(ukey).setValue(tm);
                                                                                    sharedPreferences = getSharedPreferences("SystemPre", MODE_PRIVATE);
                                                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                                    editor.putBoolean("isLogin", true);
                                                                                    editor.putString("email", email);
                                                                                    editor.putString("tcName", tname);
                                                                                    editor.putString("key", ukey);
                                                                                    editor.putString("name", name);
                                                                                    editor.putString("phone", phone);
                                                                                    editor.putLong("lastLogin", System.currentTimeMillis());
                                                                                    editor.apply();
                                                                                    Log.i("Email Verify", "True");
                                                                                    // Sign in success, update UI with the signed-in user's information
                                                                                    Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                } else {
                                                                                    Log.i("Email Verify", "False");
                                                                                }
                                                                            }
                                                                        }, 0, 800);
                                                                        binging.progressBar2.setVisibility(View.GONE);
                                                                    } else {
                                                                        Toast.makeText(SignupActivity.this, "Please Verify Your Email (" + email + ")", Toast.LENGTH_SHORT).show();
                                                                        binging.progressBar2.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });

                                                        } else {
                                                            // If sign in fails, display a message to the user.
                                                            Toast.makeText(SignupActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                                            binging.progressBar2.setVisibility(View.GONE);
                                                        }
                                                    }
                                                });
                                    }else{
                                        binging.tilSPhoneNumber.setError("Phone Number Already Registered");
                                        binging.progressBar2.setVisibility(View.GONE);
                                    }
                                }else{
                                    binging.tilSTcName.setError("Tuition Classes Name Already Registered");
                                    binging.progressBar2.setVisibility(View.GONE);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        // Finished
                    } else {
                            Toast.makeText(SignupActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(SignupActivity.this, "Phone Number least 10 character long", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SignupActivity.this, "Password least 6 character long", Toast.LENGTH_SHORT).show();
                }
                } else{
                    Toast.makeText(SignupActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
                }
        }
        }
    }
    private boolean CheckAllFields() {
        if(binging.edClassesName.getText().toString().isBlank()){
            binging.tilSTcName.setError("Tuition Classes Name is Required");
            return false;
        }if(binging.edName.getText().toString().isBlank()){
            binging.tilSFullName.setError("Full Name is Required");
            return false;
        }
        if(binging.edEmail.getText().toString().isBlank()){
            binging.tilSEmail.setError("Email is Required");
            return false;
        }
        if(binging.edPhone.getText().toString().isBlank()){
            binging.tilSPhoneNumber.setError("Phone Number is Required");
            return false;
        }if(binging.edPassword.getText().toString().isBlank()){
            binging.tilSPassword.setError("Password is Required");
            return false;
        }if(binging.sStd.getText().toString().isBlank()){
            Toast.makeText(this, "Please Select Standards", Toast.LENGTH_SHORT).show();
            return false;
        }if(binging.sSub.getText().toString().isBlank()){
            Toast.makeText(this, "Please Select Subjects", Toast.LENGTH_SHORT).show();
            return false;
        }
        binging.tilSTcName.setError("");
        binging.tilSFullName.setError("");
        binging.tilSEmail.setError("");
        binging.tilSPhoneNumber.setError("");
        binging.tilSPassword.setError("");
        return true;
    }
    public static boolean isValidEmail(String email) {
        // Regular expression pattern for email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        // Compile the regular expression pattern
        Pattern pattern = Pattern.compile(emailPattern);

        // Match the email address against the regular expression pattern
        Matcher matcher = pattern.matcher(email);

        // Return true if the email address matches the pattern, false otherwise
        return matcher.matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        // Create an instance of PhoneNumberUtil
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        try {
            // Parse the phone number with the country code for India
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "IN");

            // Check if the phone number is valid
            return phoneNumberUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            // Invalid phone number format
            return false;
        }
    }
}
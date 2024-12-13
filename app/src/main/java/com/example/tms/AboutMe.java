package com.example.tms;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.tms.databinding.FragmentAboutMeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AboutMe extends Fragment {
    private DatabaseReference annReference=FirebaseDatabase.getInstance().getReference().child("Teachers").child("Announcements");
    private DatabaseReference matReference = FirebaseDatabase.getInstance().getReference().child("Teachers").child("Materials");
    private FragmentAboutMeBinding binding;
    private final DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference().child("Teachers");
    private DatabaseReference studatabaseReference = FirebaseDatabase.getInstance().getReference().child("Students");
    private DatabaseReference imgdatabaseReference;
    private String tcName,fullName,phone,key;
    static ArrayList<Integer> sublist = new ArrayList<>();
    static List<Integer> stdlist = new ArrayList<>();
    static String[] subArray = {"Gujarati", "English", "Maths", "Science", "Social Science", "Hindi", "Environment","Sanskrit","Computer"};
    static String[] stdArray = {"1","2","3","4","5","6","7","8","9","10"};
    static boolean[] selectedSub;
    static boolean[] selectedStd;
    static String finalSelectedSub = "";
    static String finalSelectedStd = "";
    static ArrayList<String> dbSubjects = new ArrayList<>();
    static ArrayList<String> dbStandards = new ArrayList<>();
    boolean isAllFilled = false;
    private SharedPreferences sharedPreferences;
    private StorageReference storageReference;
    public Uri imageUri;
    private static TeacherModel tm = new TeacherModel();
    private String oldName="";


    public AboutMe() {}
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("SystemPre", MODE_PRIVATE);
        key = sharedPreferences.getString("key","");
        selectedSub = new boolean[subArray.length];
        selectedStd = new boolean[stdArray.length];
        imgdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Teachers").child(key).child("ImageUrl");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void uploadToFirebase(Uri uri){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading Image...");
        final StorageReference imgReference = storageReference.child(System.currentTimeMillis() + "."+ getFileExtension(uri));
        imgReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String key = imgdatabaseReference.push().getKey();
                        imgdatabaseReference.child(key).setValue(uri.toString());
                        pd.hide();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double prograssper = (100.00* snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage: "+(int) prograssper +"%");
                pd.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.hide();
                Toast.makeText(getContext(), "Failed to Upload...", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getFileExtension(Uri fileUri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(fileUri));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAboutMeBinding.inflate(inflater,container,false);
        binding.progressBarAboutme.setVisibility(View.VISIBLE);
        Glide.with(getContext()).load(R.mipmap.user).into(binding.imgTeacher);
        imgdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (getActivity() == null) {
                        return;
                    }
                    String uri = dataSnapshot.getValue().toString();
                    Glide.with(getActivity().getApplicationContext()).load(uri).into(binding.imgTeacher);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent data = result.getData();
                    imageUri = data.getData();
                    binding.imgTeacher.setImageURI(imageUri);
                }
                else {
                    Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.imgTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent();
                photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        binding.btnUploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri!=null){
                    uploadToFirebase(imageUri);
                }else{
                    Toast.makeText(getContext(), "Please Select Image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(dataSnapshot.getKey().equals(key)){
                        tm.setFullName((String) dataSnapshot.child("fullName").getValue());
                        tm.setTcName((String) dataSnapshot.child("tcName").getValue());
                        tm.setPhone((String) dataSnapshot.child("phone").getValue());
                        dbSubjects = ((ArrayList<String>) dataSnapshot.child("subjects").getValue());
                        dbStandards = ((ArrayList<String>) dataSnapshot.child("standards").getValue());
                    }
                }
                binding.eduName.setText(tm.getFullName());
                binding.eduTcName.setText(tm.getTcName());
                binding.eduPhoneNumber.setText(tm.getPhone());
                oldName=binding.eduTcName.getText().toString();
                binding.spUSub.setText(""+dbSubjects);
                binding.spUStd.setText(""+dbStandards);
                for (int j=0;j<subArray.length;j++){
                    if(dbSubjects.contains(subArray[j])){
                        selectedSub[j]=true;
                    }
                }
                for (int j=0;j<stdArray.length;j++){
                    if(dbStandards.contains(stdArray[j])){
                        selectedStd[j]=true;
                    }
                }
                binding.progressBarAboutme.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.txtBackinAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new AccountFragment());
            }
        });
        //Subjects Popup
        binding.spUSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                // set title
                builder.setTitle("Select Subjects");
                // set dialog non cancelable
                builder.setCancelable(false);
                builder.setMultiChoiceItems(subArray, selectedSub, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            selectedSub[i] = true;
                        } else {
                            selectedSub[i] = false;
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < selectedSub.length; j++) {
                            if(selectedSub[j]){
                                stringBuilder.append(subArray[j]+", ");
                            }

                        }
                        finalSelectedSub = stringBuilder.toString();
                        binding.spUSub.setText(""+finalSelectedSub);
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
                            binding.spUSub.setText("");
                            finalSelectedSub = "";
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
        // Standards Popup
        binding.spUStd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select Standard");
                // set dialog non cancelable
                builder.setCancelable(false);
                builder.setMultiChoiceItems(stdArray, selectedStd, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            selectedStd[i] = true;
                        } else {
                            selectedStd[i] = false;
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < selectedStd.length; j++) {
                            if(selectedStd[j]){
                                stringBuilder.append(stdArray[j]+", ");
                            }
                        }
                        finalSelectedStd = stringBuilder.toString();
                        binding.spUStd.setText(""+finalSelectedStd);
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
                            binding.spUStd.setText("");
                            finalSelectedStd = "";
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });

        //Save Button Clicked

        binding.btnUpdateTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullName = binding.eduName.getText().toString();
                phone = binding.eduPhoneNumber.getText().toString();
                tcName = binding.eduTcName.getText().toString();
                finalSelectedStd = binding.spUStd.getText().toString();
                finalSelectedSub = binding.spUSub.getText().toString();
                if(fullName.isBlank() && tcName.isBlank() && phone.isBlank() && finalSelectedSub.isBlank() && finalSelectedStd.isBlank()){
                    binding.tiluName.setError("Full Name is Required");
                    binding.tiluPhoneNumber.setError("Phone Number is Required");
                    binding.tilutcName.setError("Tuition Classes Name is Required");
                    Toast.makeText(getContext(), "Please Select Subjects or Standards", Toast.LENGTH_SHORT).show();
                    isAllFilled = false;
                }else{
                    if(fullName.isBlank()){
                        isAllFilled = false;
                        binding.tiluName.setError("FullName is Required");
                    }else{
                        isAllFilled = true;
                        binding.tiluName.setError("");
                    }
                    if(phone.isBlank()){
                        isAllFilled = false;
                        binding.tiluPhoneNumber.setError("Phone Number is Required");
                    }else{
                        isAllFilled = true;
                        binding.tiluPhoneNumber.setError("");
                    }
                    if(tcName.isBlank()){
                        isAllFilled = false;
                        binding.tilutcName.setError("Tuition Classes Name is Required");
                    }else{
                        isAllFilled = true;
                        binding.tilutcName.setError("");
                    }
                    if(finalSelectedStd.isBlank()){
                        isAllFilled = false;
                        Toast.makeText(getContext(), "Please Select Standards", Toast.LENGTH_SHORT).show();
                    }else{
                        isAllFilled = true;
                    }
                    if(finalSelectedSub.isBlank()){
                        isAllFilled = false;
                        Toast.makeText(getContext(), "Please Select Subjects", Toast.LENGTH_SHORT).show();
                    }else{
                        isAllFilled = true;
                    }
                    if(isAllFilled){
                        finalSelectedSub = finalSelectedSub.replace("[","");
                        finalSelectedSub = finalSelectedSub.replace("]","");
                        finalSelectedStd = finalSelectedStd.replace("[","");
                        finalSelectedStd = finalSelectedStd.replace("]","");
                        String[] finalsubs = finalSelectedSub.split(", ");
                        ArrayList<String> sublist = new ArrayList<String>(Arrays.asList(finalsubs));
                        String[] finalstds = finalSelectedStd.split(", ");
                        ArrayList<String> stdlist = new ArrayList<String>(Arrays.asList(finalstds));
                        if(isValidPhoneNumber(phone)){

                        }else{
                            binding.tiluPhoneNumber.setError("Phone Number is invalid");
                        }

                        SharedPreferences.Editor  editor = sharedPreferences.edit();
                        editor.putString("name",fullName);
                        editor.putString("phone",phone);
                        
                        databaseReference.child(key).child("fullName").setValue(fullName);
                        databaseReference.child(key).child("phone").setValue(phone);
                        databaseReference.child(key).child("standards").setValue(stdlist);
                        databaseReference.child(key).child("subjects").setValue(sublist);


                        // Update Tc name n Ann. List
                        annReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    if(dataSnapshot.child("tcName").getValue().toString().equals(oldName)){
                                        String key = dataSnapshot.getKey();
                                        annReference.child(key).child("tcName").setValue(tcName);
                                        annReference.child(key).child("fullName").setValue(fullName);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        // Update Tc Name in Materials List
                        matReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    if(dataSnapshot.child("tcName").getValue().toString().equals(oldName)){
                                        String key = dataSnapshot.getKey();
                                        matReference.child(key).child("tcName").setValue(tcName);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        // Update Tc Name is Student List
                        studatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
//                                    Log.i("Student Key",""+key);
//                                    Log.i("old Tcname",""+tm.getTcName());
//                                    Log.i("DB Tcname",""+dataSnapshot.child("tcName").getValue().toString());
                                    if(dataSnapshot.child("tcName").getValue().toString().equals(oldName)){
                                        String key = dataSnapshot.getKey();
                                        studatabaseReference.child(key).child("tcName").setValue(tcName);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                        editor.putString("tcName",tcName);
                        databaseReference.child(key).child("tcName").setValue(tcName);
                        editor.commit();
                        Toast.makeText(getContext(), "Data Successfully Updated...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
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
package com.example.tms;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

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

public class StudentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<StudentModel> studentModels;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Students");
    String[] subArray = {"Gujarati", "English", "Maths", "Science", "Social Science", "Hindi", "Environment", "Sanskrit", "Computer"};
    ArrayList<Integer> sublist = new ArrayList<>();
    boolean[] selectedSub;
    String finalSelectedSub = "";
    TextInputLayout tilname,tilphone;
    private DatabaseReference imgdatabaseReference;
    public StudentAdapter(Context context, ArrayList<StudentModel> studentModels) {
        this.context = context;
        this.studentModels = studentModels;
    }

    @Override
    public int getCount() {
        return studentModels.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("SystemPre",MODE_PRIVATE);
//        String email  = sharedPreferences.getString("email","");
//        String password  = sharedPreferences.getString("password","");
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View view1 = inflater.inflate(R.layout.studentlist,null);
        TextView txtStudentName = view1.findViewById(R.id.txtStudentName);
        TextView txtPhoneNumber = view1.findViewById(R.id.txtPhoneNumber);
        TextView txtStd = view1.findViewById(R.id.txtStd);
        TextView txtsub = view1.findViewById(R.id.txtStuSubjects);
        ImageView im = view1.findViewById(R.id.ivStudent);
        imgdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Students").child(studentModels.get(i).getKey()).child("Image");
        imgdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(context==null){
                    return;
                }
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    String uri = dataSnapshot.getValue().toString();
                    Glide.with(context).load(uri).into(im);
//                    Log.i("ImageUri",""+uri.toString());

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
//        imageView.setImageResource(studentimages[i]);
        txtStudentName.setText(studentModels.get(i).getFullName());
        txtPhoneNumber.setText(studentModels.get(i).getPhoneNumber());
        txtStd.setText(studentModels.get(i).getStandards());
        txtsub.setText(""+studentModels.get(i).getSubjects());

        // Set Edit and Delete Data
        ConstraintLayout layout = view1.findViewById(R.id.singleCardofStudent);
        layout.setOnClickListener(new View.OnClickListener() {
            boolean isTextViewClicked = false;
            Button btnEdit;
            EditText txtFullName,txtPhone;
            Spinner spEditSTD;
            TextView spSubs;

            @Override
            public void onClick(View view) {
                selectedSub = new boolean[subArray.length];
                String[] stds = {"Select Standards","1","2","3","4","5","6","7","8","9","10"};
                LayoutInflater inflater = LayoutInflater.from(view.getRootView().getContext());
                View view1 = inflater.inflate(R.layout.editstudent, null);
                AlertDialog builder = new AlertDialog.Builder(view.getRootView().getContext()).create();
                builder.setCancelable(true);
                builder.setView(view1);
                builder.setTitle("Edit Student");
                txtFullName = view1.findViewById(R.id.editFullName);
                txtPhone = view1.findViewById(R.id.editPhoneNumber);
                spEditSTD = view1.findViewById(R.id.spEditStd);
                spSubs = view1.findViewById(R.id.editSubs);
                btnEdit = view1.findViewById(R.id.btnEditStudent);
                tilname = view1.findViewById(R.id.tilFullName);
                tilphone = view1.findViewById(R.id.tilPhone);
                ArrayAdapter arrayAdapter = new ArrayAdapter(view.getRootView().getContext(), android.R.layout.simple_list_item_1,stds);
                spEditSTD.setAdapter(arrayAdapter);
                spEditSTD.setSelection(Integer.parseInt(studentModels.get(i).getStandards()));
                txtFullName.setText(studentModels.get(i).getFullName());
                txtPhone.setText(studentModels.get(i).getPhoneNumber());
                spSubs.setText(""+studentModels.get(i).getSubjects());
                finalSelectedSub=""+studentModels.get(i).getSubjects();
                for (int j=0;j<subArray.length;j++){
                   if((studentModels.get(i).getSubjects().contains(subArray[j]))){
                        selectedSub[j]=true;
                    }
                }
                builder.show();
                spSubs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isTextViewClicked = true;
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
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
                                    selectedSub[i]=true;
                                } else {
                                    selectedSub[i]=false;
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
                                spSubs.setText(""+finalSelectedSub);
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
                                    spSubs.setText("");
                                    finalSelectedSub = "";
                                }
                            }
                        });
                        // show dialog
                        builder.show();
                    } // end of onclick spsubs
                });

                btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String uFullName = txtFullName.getText().toString();
                        String uphone = txtPhone.getText().toString();
                        String uStd = spEditSTD.getSelectedItem().toString();
                        String[] finalsubs = finalSelectedSub.split(", ");
                        ArrayList<String> sublist = new ArrayList<String>(Arrays.asList(finalsubs));
                        boolean isAllFilled = false ;
                        if(uFullName.isBlank() && uphone.isBlank() && uStd.equals("Select Standards") && finalSelectedSub.isBlank()){
                            tilname.setError("Full Name is Required");
                            tilphone.setError("Phone Number is Required");
                            Toast.makeText(context, "Please Select Standards", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, "Please Select Subjects...", Toast.LENGTH_SHORT).show();
                            isAllFilled = false;
                        }else{
                            tilname.setError("");
                            tilphone.setError("");
                            if(CheckAllFields(uFullName,uphone,uStd,finalSelectedSub)){
                                if(isValidPhoneNumber(uphone) && uphone.length()==10){
                                    databaseReference.child(studentModels.get(i).getKey()).child("fullName").setValue(uFullName);
                                    databaseReference.child(studentModels.get(i).getKey()).child("phoneNumber").setValue(uphone);
                                    databaseReference.child(studentModels.get(i).getKey()).child("standards").setValue(uStd);
                                    if(!isTextViewClicked){
                                        databaseReference.child(studentModels.get(i).getKey()).child("subjects").setValue(studentModels.get(i).getSubjects());
                                    }else{
                                        databaseReference.child(studentModels.get(i).getKey()).child("subjects").setValue(sublist);
                                    }
                                    StudentsFragment.adapter.notifyDataSetChanged();
                                    builder.dismiss();
                                    isTextViewClicked = false;
                                }else{
                                    tilphone.setError("Phone Number is invalid");
                                }
                            }
                        }
                    }
                });
            }
        });
        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setMessage("Are You Want to Sure Delete Data of "+studentModels.get(i).getFullName());
                builder.setTitle("Alert !");
                builder.setCancelable(true);
                //Ask to ma'am
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    databaseReference.child(studentModels.get(i).getKey()).child("status").setValue("Disable");
                    StudentsFragment.studentModels.clear();
                    StudentsFragment.adapter.notifyDataSetChanged();
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    dialog.cancel();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
        return view1;
    }

    private boolean CheckAllFields(String uFullName, String uphone,String ustd,String finalSelectedSub) {
        if(uFullName.isBlank()){
            tilname.setError("Full Name is Required");
            return false;
        }if(uphone.isBlank()){
           tilphone.setError("Phone Number is Required");
            return false;
        }
        if(ustd.equals("Select Standards")){
            Toast.makeText(context, "Please Select Standards", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(finalSelectedSub.isBlank()){
            Toast.makeText(context, "Please Select Subjects", Toast.LENGTH_SHORT).show();
            return false;
        }
        tilname.setError("");
        tilphone.setError("");
        return true;
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

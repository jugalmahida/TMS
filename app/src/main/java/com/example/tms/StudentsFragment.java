package com.example.tms;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.tms.databinding.FragmentStudentsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
public class StudentsFragment extends Fragment {
    public StudentsFragment() {}
    private FragmentStudentsBinding binding;
    public static ArrayList<StudentModel> studentModels = new ArrayList<>();
    public static StudentAdapter adapter;
//    SharedPreferences sharedPreferences ;
    private DatabaseReference studentref;
    private String tcNamep,key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        sharedPreferences = getActivity().getSharedPreferences("SystemPre", MODE_PRIVATE);
        studentModels.clear();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStudentsBinding.inflate(inflater,container,false);
        getStudents();
        binding.progressBarStudent.setVisibility(View.VISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvgoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new AccountFragment());
            }
        });
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }

    private void getStudents(){
        studentref = FirebaseDatabase.getInstance().getReference("Students");
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SystemPre",MODE_PRIVATE);
        tcNamep = sharedPreferences.getString("tcName","");
        key = sharedPreferences.getString("key","");
        studentref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentModels.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    StudentModel sm = new StudentModel();
                    if(dataSnapshot.child("tcName").getValue().toString().equals(tcNamep) && (!dataSnapshot.child("status").getValue().equals("Disable"))) {
                        sm.setKey(dataSnapshot.getKey());
                        sm.setTcName((String) dataSnapshot.child("tcName").getValue());
                        sm.setEmail((String) dataSnapshot.child("email").getValue());
                        sm.setFullName((String) dataSnapshot.child("fullName").getValue());
                        sm.setPhoneNumber((String) dataSnapshot.child("phoneNumber").getValue());
                        sm.setStandards((String) dataSnapshot.child("standards").getValue());
                        sm.setSubjects((ArrayList<String>) dataSnapshot.child("subjects").getValue());
                        Object imageValue = dataSnapshot.child("Image").getValue();
                        if(!(imageValue ==null)){
                            sm.setImgUrl(imageValue.toString());
                        }
                        studentModels.add(sm);
                    }
                }
                if(studentModels.isEmpty()){
                    binding.textViewNothingStudent.setVisibility(View.VISIBLE);
                }else{
                    binding.textViewNothingStudent.setVisibility(View.GONE);
                }
                adapter = new StudentAdapter(getContext(), studentModels);
                binding.studentList.setAdapter(adapter);
                binding.progressBarStudent.setVisibility(View.GONE);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
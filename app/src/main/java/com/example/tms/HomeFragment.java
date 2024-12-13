package com.example.tms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.tms.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private DatabaseReference annReference=FirebaseDatabase.getInstance().getReference().child("Teachers").child("Announcements");;
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    public static RecyclerView.Adapter adapter;
    SharedPreferences sharedPreferences ;
    public HomeFragment() {}
    public static List<AnnouncementModel> modelList = new ArrayList<>();
    String tcName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences("SystemPre", Context.MODE_PRIVATE);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        binding.progressBarHome.setVisibility(View.VISIBLE);
        modelList.clear();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        long lastLoginTimestamp = sharedPreferences.getLong("lastLogin", 0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String lastLoginTime = dateFormat.format(new Date(lastLoginTimestamp));
        binding.txtLastLogin.setText(lastLoginTime);
        annReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tcName = sharedPreferences.getString("tcName","");
                modelList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    AnnouncementModel model = new AnnouncementModel();
//                    Log.i("Tname",dataSnapshot.child("tcName").getValue().toString());
//                    Log.i("cutname",tcName);
                    if(dataSnapshot.child("tcName").getValue().toString().equals(tcName)){
                        model.setId(dataSnapshot.getKey());
                        model.setFullName((String) dataSnapshot.child("fullName").getValue());
                        model.setAnnouncement((String) dataSnapshot.child("announcement").getValue());
                        model.setTime((String) dataSnapshot.child("time").getValue());
                        modelList.add(model);
                    }
                }
                if(modelList.isEmpty()){
                    binding.textViewNothingAnn.setVisibility(View.VISIBLE);
                }else{
                    binding.textViewNothingAnn.setVisibility(View.GONE);
                }
                Collections.reverse(modelList);
                tcName = sharedPreferences.getString("tcName","");
                binding.txtTcName.setText(tcName);
                recyclerView = view.findViewById(R.id.cardRecycler);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                adapter = new AnnouncementAdapter(getContext(),modelList);
                recyclerView.setAdapter(adapter);
                binding.progressBarHome.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        binding.edNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnSubmit.setVisibility(View.VISIBLE);
                binding.btnCancel.setVisibility(View.VISIBLE);
            }
        });
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String notice = binding.edNotice.getText().toString().trim();
                if(notice.isBlank()){
                    binding.tilnotice.setError("This Announcement is Required");
                }else{
                    binding.tilnotice.setError("");
                    Date currentDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                    String dateString = dateFormat.format(currentDate);
                    String name = sharedPreferences.getString("name","");
                    String tcName = sharedPreferences.getString("tcName","");
                    AnnouncementModel announcementModel = new AnnouncementModel(name,dateString,notice,tcName);
                    String key = annReference.push().getKey();
                    annReference.child(key).setValue(announcementModel);
                    binding.edNotice.setText("");
                    HashSet<AnnouncementModel> announcementModels = new HashSet<>();
                    modelList.clear();
                    announcementModels.addAll(announcementModels);
                    adapter.notifyDataSetChanged();
                    hideKeyboard(getActivity());
                }
            }
        });
        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnSubmit.setVisibility(View.GONE);
                binding.btnCancel.setVisibility(View.GONE);
                hideKeyboard(getActivity());
            }
        });
    }
    public static void hideKeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
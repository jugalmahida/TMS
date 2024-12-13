package com.example.tms;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tms.databinding.FragmentAcademicsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AcademicsFragment extends Fragment {
    private FragmentAcademicsBinding binding;
    private Uri fileUri;
    private String fileName,tcName;
    private Button btnSelectaFile,btnSubmit;
    private EditText file_Name;
    private StorageReference storageReference;
    private DatabaseReference rootDatabaseReference;
    AlertDialog builder;
    public static ArrayList<MaterialModel> materialModels = new ArrayList<>();
    public static MaterialAdapter adapter;
    SharedPreferences sharedPreferences;
    private String selectedFileName;
    public AcademicsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference();
        rootDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Teachers").child("Materials");
        sharedPreferences = getActivity().getSharedPreferences("SystemPre", Context.MODE_PRIVATE);
        tcName = sharedPreferences.getString("tcName","");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAcademicsBinding.inflate(inflater,container,false);
        materialModels.clear();
        binding.progressBarAca.setVisibility(View.VISIBLE);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.txtTCName.setText(tcName);
        rootDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                materialModels.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    MaterialModel model = new MaterialModel();
//                    Log.i("Shared Pre tcName",""+tcName);
//                    Log.i("Db name tcName",""+dataSnapshot.child("tcName").getValue().toString());
                    if(dataSnapshot.child("tcName").getValue().toString().equals(tcName)){
                        model.setFileName((String) dataSnapshot.child("fileName").getValue());
                        model.setTime((String) dataSnapshot.child("time").getValue());
                        model.setTcName((String) dataSnapshot.child("tcName").getValue());
                        model.setFileUri((String) dataSnapshot.child("fileUri").getValue());
                        model.setFileId(dataSnapshot.getKey());
                        materialModels.add(model);
                    }
                }
                if(materialModels.isEmpty()){
                    binding.textViewNothingAca.setVisibility(View.VISIBLE);
                }else{
                    binding.textViewNothingAca.setVisibility(View.GONE);
                }
                Collections.reverse(materialModels);
//                Log.i("List of Mat",""+materialModels);
                adapter = new MaterialAdapter(getContext(), materialModels);
                binding.matlist.setAdapter(adapter);
                binding.progressBarAca.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent data = result.getData();
                    fileUri = data.getData();
                    Cursor returnCursor = getActivity().getContentResolver().query(fileUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    selectedFileName = returnCursor.getString(nameIndex);
                    btnSelectaFile.setText(selectedFileName);
                }
                else {
                    Toast.makeText(getContext(), "No File Selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.fabAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View view1 = inflater.inflate(R.layout.addfilelayout, null);
                btnSelectaFile = view1.findViewById(R.id.btnSelectFile);
                btnSubmit = view1.findViewById(R.id.btnFileSubmit);
                file_Name = view1.findViewById(R.id.txtFileName);
                builder = new AlertDialog.Builder(getContext()).create();
                builder.setCancelable(true);
                builder.setView(view1);
                builder.setTitle("Add an New Material");
                builder.show();
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileName = file_Name.getText().toString().trim();
                        if(fileUri!=null){
                            uploadToFirebase(fileUri);
                            builder.dismiss();
//                            materialModels.clear();
                            adapter.notifyDataSetChanged();
                            fileUri = null;
                        }else{
                            Toast.makeText(getContext(), "Select a File", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                btnSelectaFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent photoPicker = new Intent();
                        photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                        photoPicker.setType("*/*");
                        activityResultLauncher.launch(photoPicker);
                    }
                });
            }
        });

    }
    private void uploadToFirebase(Uri mainuri){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading New Material...");
        pd.setCancelable(false);
        String tcName = sharedPreferences.getString("tcName","");
        final StorageReference imgReference = storageReference.child(System.currentTimeMillis() + "."+getFileExtension(mainuri));
        imgReference.putFile(mainuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imgReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Date currentDate = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                        String dateString = dateFormat.format(currentDate);
                        String key = rootDatabaseReference.push().getKey();
                        if(fileName.isBlank()){
                            fileName = selectedFileName;
                        }
                        if(!fileName.contains(".")){
                            fileName = fileName+"."+getFileExtension(mainuri);
                        }
                        MaterialModel model = new MaterialModel(uri.toString(),fileName,dateString,tcName,key);
                        rootDatabaseReference.child(key).setValue(model);
                        pd.hide();
                        Toast.makeText(getContext(), "New Materials Added...", Toast.LENGTH_SHORT).show();
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



}
package com.example.tms;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class MaterialAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<MaterialModel> materialModels;
//    private FirebaseStorage storage = FirebaseStorage.getInstance();
//    private StorageReference storageReference = storage.getReference().child("Teachers").child("Materials");
    DatabaseReference rootDatabaseReference = FirebaseDatabase.getInstance().getReference();
    DatabaseReference subDirRef = rootDatabaseReference.child("Teachers").child("Materials");
    public MaterialAdapter(Context context, ArrayList<MaterialModel> materialModels) {
        this.context = context;
        this.materialModels = materialModels;
    }

    @Override
    public int getCount() {
        return materialModels.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View view1 = inflater.inflate(R.layout.materiallist,null);
        TextView txtFilename = view1.findViewById(R.id.txtfilename);
        TextView txtTime = view1.findViewById(R.id.txttime);
        ConstraintLayout layout = view1.findViewById(R.id.mainMaterial);
        ImageButton btnDonwnload = view1.findViewById(R.id.btnImgDownload);
        txtFilename.setText(materialModels.get(i).getFileName());
        txtTime.setText(materialModels.get(i).getTime());
        Log.i("Tcname",""+materialModels.get(i).getTcName());
        btnDonwnload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subDirRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot subDirSnapshot : snapshot.getChildren()) {
                            if(subDirSnapshot.child("fileName").getValue().equals(materialModels.get(i).getFileName())){
                                String dbFileName = subDirSnapshot.child("fileName").getValue().toString();
                                DatabaseReference urlRef = subDirSnapshot.getRef().child("fileUri");
                                urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String url = dataSnapshot.getValue(String.class);
//                                    String key = subDirSnapshot.getRef().child("fileUri").toString();
//                                        Log.i("URL-----", "" + url);
//                                        Log.i("URL-----", "" + dbFileName);
//                                    Toast.makeText(context, "..."+dbFileName, Toast.LENGTH_SHORT).show();
                                        downloadFile(url,dbFileName);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("Error-----", databaseError.getMessage());
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setMessage("Are You Want to Sure Delete File Named - \n"+materialModels.get(i).getFileName());
                builder.setTitle("Alert !");
                builder.setCancelable(true);

                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    StorageReference file = FirebaseStorage.getInstance().getReferenceFromUrl(materialModels.get(i).getFileUri());
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Teachers").child("Materials");
                    file.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            databaseReference.child(materialModels.get(i).getFileId()).removeValue();
                            AcademicsFragment.materialModels.clear();
                            AcademicsFragment.adapter.notifyDataSetChanged();
                            Toast.makeText(context, "File Deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
        return view1;
    }

    private void downloadFile(String url, String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (file.exists()) {
            Toast.makeText(context, "File Already Downloaded", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            Toast.makeText(context, "File Downloaded", Toast.LENGTH_SHORT).show();
        }

//        Toast.makeText(context, "Download File Called", Toast.LENGTH_SHORT).show();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);
        request.setDescription("Downloading file...");

        // set the destination folder and file name
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // get download service and enqueue file
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }
}
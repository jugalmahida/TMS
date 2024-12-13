package com.example.tms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder>{
    Context context;
    private List<AnnouncementModel> list;
    private DatabaseReference annReference = FirebaseDatabase.getInstance().getReference().child("Teachers").child("Announcements");
    DatabaseReference imgReference;
    public AnnouncementAdapter(Context context, List<AnnouncementModel> list) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public AnnouncementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.card,parent,false);
         SharedPreferences sharedPreferences = context.getSharedPreferences("SystemPre",Context.MODE_PRIVATE);
         String key = sharedPreferences.getString("key","");
         imgReference=FirebaseDatabase.getInstance().getReference().child("Teachers").child(key).child("ImageUrl");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementAdapter.ViewHolder holder, int position) {
        AnnouncementModel announcementModel = list.get(position);
        holder.txtfullName.setText(announcementModel.getFullName());
        holder.txtTime.setText(announcementModel.getTime());
        holder.txtAnn.setText(announcementModel.getAnnouncement());
        holder.id = announcementModel.getId();

        imgReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (context == null) {
                        return;
                    }
                    Object obj = dataSnapshot.getValue();
                    if(obj==null){
                        Glide.with(context.getApplicationContext()).load(R.mipmap.userprofile).into(holder.imgDp);
                    }else{
                        String uri =dataSnapshot.getValue().toString();
                        Glide.with(context.getApplicationContext()).load(uri).into(holder.imgDp);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        holder.itemView.setTag(announcementModel);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView txtfullName,txtTime,txtAnn;
        public String id;
        public ImageView imgDp;
        private ConstraintLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtfullName = itemView.findViewById(R.id.txtName);
            txtTime = itemView.findViewById(R.id.txtUTime);
            txtAnn = itemView.findViewById(R.id.txtAnn);
            imgDp = itemView.findViewById(R.id.ivDp);
            layout = itemView.findViewById(R.id.conAnnLayout);

            layout.setOnClickListener(new View.OnClickListener() {
                EditText ann;
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    @SuppressLint("ViewHolder") View view1 = inflater.inflate(R.layout.editannouncement,null);

                    AlertDialog builder = new AlertDialog.Builder(view.getRootView().getContext()).create();
                    builder.setCancelable(true);
                    builder.setView(view1);
                    builder.setTitle("Edit Announcement");
                    ann  = view1.findViewById(R.id.ededitAnn);
                    ann.setText(txtAnn.getText().toString().trim());
                    builder.show();

                    Button btneditAnn = view1.findViewById(R.id.btnEditAnnouncement);
                    TextInputLayout textInputLayout = view1.findViewById(R.id.editAnnouncement);
                    btneditAnn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String uann = ann.getText().toString().trim();
                            if(uann.isBlank()){
                                textInputLayout.setError("This Field Is Required");
                            }else{
                                textInputLayout.setError("");
//                                Toast.makeText(context, ""+id, Toast.LENGTH_SHORT).show();
                                annReference.child(id).child("announcement").setValue(uann);
                                HomeFragment.adapter.notifyDataSetChanged();
                                builder.dismiss();
                            }
                        }
                    });
                }
            });
            layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                    builder.setMessage("Are You Want to Sure Delete This Announcement");
                    builder.setTitle("Alert !");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        annReference.child(id).removeValue();
                        HomeFragment.adapter.notifyDataSetChanged();
                        dialog.cancel();
                    });
                    builder.setNegativeButton("No", (dialog, which) -> {
                        dialog.cancel();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                }
            });
        }
    }
}

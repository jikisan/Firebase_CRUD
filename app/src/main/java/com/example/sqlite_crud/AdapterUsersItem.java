package com.example.sqlite_crud;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AdapterUsersItem extends RecyclerView.Adapter<AdapterUsersItem.ItemViewHolder> {

    private List<Users> arr;
    private OnItemClickListener onItemClickListener;

    public AdapterUsersItem() {
    }

    public AdapterUsersItem(List<Users> arr) {
        this.arr = arr;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterUsersItem.ItemViewHolder
                (LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        Users users = arr.get(position);
        holder.tv_firstName.setText(users.firstName);
        holder.tv_lastName.setText(users.lastName);
        holder.tv_contactNum.setText(users.contactNum);
        holder.tv_email.setText(users.email);

        String imageUrl = users.imageUrl;

        if(!imageUrl.isEmpty())
        {
            Picasso.get()
                    .load(imageUrl)
                    .into(holder.circleImageView);
        }

        String userID = users.userId;
        String name = users.firstName + " " + users.lastName;


        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("Users");


                new SweetAlertDialog(view.getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("DELETE USER")
                        .setCancelText("Back")
                        .setConfirmButton("Remove", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                Query query = userDB.orderByChild("firstName")
                                        .equalTo(users.firstName);

                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren())
                                        {
                                            dataSnapshot.getRef().removeValue();
                                            Intent intent = new Intent(view.getContext(), dashboard_two.class);
                                            view.getContext().startActivity(intent);

                                        }
                                        notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })
                        .setContentText("Delete " + name + "?")
                        .show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return arr.size();
    }

    public interface  OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tv_firstName, tv_lastName, tv_contactNum, tv_email;
        ImageView circleImageView, iv_delete;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_firstName = itemView.findViewById(R.id.tv_firstName);
            tv_lastName = itemView.findViewById(R.id.tv_lastName);
            tv_contactNum = itemView.findViewById(R.id.tv_contactNum);
            tv_email = itemView.findViewById(R.id.tv_email);

            circleImageView = itemView.findViewById(R.id.circleImageView);
            iv_delete = itemView.findViewById(R.id.iv_delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(onItemClickListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}

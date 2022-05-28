package com.example.sqlite_crud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class dashboard_two extends AppCompatActivity {

    private List<Users> arrUsers, arr;
    private List<String> arrKey;
    private AdapterUsersItem adapterUsersItem, adapter;
    private FirebaseAuth fAuth;
    private FirebaseUser user;
    private DatabaseReference userDB;

    private TextView tv_back;
    private SearchView sv_search;
    private RecyclerView recyclerView_searches;
    private Button btn_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_two);


        user = FirebaseAuth.getInstance().getCurrentUser();
        userDB = FirebaseDatabase.getInstance().getReference("Users");


        setRef();

        generateRecyclerLayout();
        generateDataValue();
        clickListeners();
    }

    private void generateDataValue() {
        if(userDB != null)
        {
            getViewHolderValues();

        }
        if(sv_search != null)
        {
            sv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);
                    return false;
                }
            });

        }
    }

    private void clickListeners() {
        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SweetAlertDialog(view.getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Warning")
                        .setCancelText("Back")
                        .setConfirmButton("Sign out", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                fAuth.getInstance().signOut();
                                Intent intent = new Intent(dashboard_two.this, login_page.class);
                                startActivity(intent);
                            }
                        })
                        .setContentText("Proceed with sign out?")
                        .show();


            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(dashboard_two.this, user_form.class);
               //intent.putExtra("category", "add");
                startActivity(intent);
            }
        });

        adapterUsersItem.setOnItemClickListener(new AdapterUsersItem.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                arrUsers.get(position);

                String keyID = arrKey.get(position);
                Intent intent = new Intent(dashboard_two.this, user_form.class);
                intent.putExtra("category", "edit");
                intent.putExtra("user id", keyID);
                startActivity(intent);
            }
        });
    }

    private void generateRecyclerLayout() {

        recyclerView_searches.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView_searches.setLayoutManager(linearLayoutManager);

        arrUsers = new ArrayList<>();
        arrKey = new ArrayList<>();
        adapterUsersItem = new AdapterUsersItem(arrUsers);
        recyclerView_searches.setAdapter(adapterUsersItem);


    }

    private void getViewHolderValues() {

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Users users = dataSnapshot.getValue(Users.class);

                    String keyID = dataSnapshot.getKey().toString();
                    String userID = user.getUid();
                    if(users.userId.equals(userID))
                    {
                        continue;
                    }

                    arrUsers.add(users);
                    arrKey.add(keyID);
                }
                adapterUsersItem.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void search(String s) {
        arr = new ArrayList<>();
        for(Users object : arrUsers)
        {
            if(object.getLastName().toLowerCase().contains(s.toLowerCase()))
            {
                arr.add(object);
            }

            adapter = new AdapterUsersItem(arr);
            recyclerView_searches.setAdapter(adapter);
        }

        adapter.setOnItemClickListener(new AdapterUsersItem.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                arr.get(position);
                Query query = userDB
                        .orderByChild("firstName")
                        .equalTo(arr.get(position).firstName);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot dataSnapshot : snapshot.getChildren())
                        {
                            String keyID = dataSnapshot.getKey().toString();
                            Intent intent = new Intent(dashboard_two.this, user_form.class);
                            intent.putExtra("category", "edit");
                            intent.putExtra("user id", keyID);
                            startActivity(intent);
                        }
                        adapterUsersItem.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });



    }

    private void setRef() {
        tv_back = findViewById(R.id.tv_back);
        sv_search = findViewById(R.id.sv_search);
        recyclerView_searches = findViewById(R.id.recyclerView_searches);
        btn_add = findViewById(R.id.btn_add);
    }
}
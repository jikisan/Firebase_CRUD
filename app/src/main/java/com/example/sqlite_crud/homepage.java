package com.example.sqlite_crud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class homepage extends AppCompatActivity {

    private Button btn_sqlite, btn_firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        btn_firebase = findViewById(R.id.btn_firebase);
        btn_sqlite = findViewById(R.id.btn_sqlite);

        btn_sqlite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(homepage.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        btn_firebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(homepage.this, login_page.class);
                startActivity(intent);
            }
        });


    }
}
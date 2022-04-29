package com.example.sqlite_crud;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {

    EditText et_prodName, et_prodDesc, et_prodPrice, et_prodQuantity, et_prodStatus;
    Button btn_add,btn_update,btn_delete,btn_view;
    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        setRef();
        insert();
        update();
        delete();
        view();

    }

    private void view() {
        btn_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor res = DB.getData();
                if(res.getCount()==0){
                    Toast.makeText(MainActivity2.this, "Not exist", Toast.LENGTH_SHORT).show();
                }
                StringBuffer buffer = new StringBuffer();
                while(res.moveToNext()){
                    buffer.append("User ID: " + res.getString(0)+ "\n");
                    buffer.append("First Name: " + res.getString(1)+ "\n");
                    buffer.append("Last Name: " + res.getString(2)+ "\n");
                    buffer.append("Age: " + res.getString(3)+ "\n");
                    buffer.append("Height: " + res.getString(4)+ "\n");
                    buffer.append("Address: " + res.getString(5)+ "\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity2.this);
                builder.setCancelable(true);
                builder.setTitle("Products");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
    }

    private void delete() {
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    String prodName = et_prodName.getText().toString();

                    Boolean checkdeletedata = DB.deleteUserData(prodName);
                    if(checkdeletedata == true)
                        Toast.makeText(MainActivity2.this, "Delete Successful", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity2.this, "Delete Failed", Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    System.out.println("Error Delete " + e.getMessage());
                    Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void update() {
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    String prodName = et_prodName.getText().toString();
                    String prodDesc = et_prodDesc.getText().toString();
                    int prodPrice = Integer.parseInt(et_prodPrice.getText().toString());
                    int prodQuantity = Integer.parseInt(et_prodQuantity.getText().toString());
                    String prodStatus = et_prodStatus.getText().toString();

                    Boolean checkinsertdata = DB.updateUserData(prodName, prodDesc, prodPrice, prodQuantity, prodStatus);
                    if(checkinsertdata == true)
                        Toast.makeText(MainActivity2.this, "Update Successful", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity2.this, "Update Failed", Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    System.out.println("Error Update " + e.getMessage());
                    Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void insert() {
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {

                    String prodName = et_prodName.getText().toString();
                    String prodDesc = et_prodDesc.getText().toString();
                    int prodPrice = Integer.parseInt(et_prodPrice.getText().toString());
                    int prodQuantity = Integer.parseInt(et_prodQuantity.getText().toString());
                    String prodStatus = et_prodStatus.getText().toString();

                    Boolean checkupdatedata = DB.insertUserData(prodName, prodDesc, prodPrice, prodQuantity, prodStatus);
                    if(checkupdatedata == true)
                        Toast.makeText(MainActivity2.this, "Insert Successful", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity2.this, "Insert Failed", Toast.LENGTH_SHORT).show();


                }catch (Exception e){
                    System.out.println("Error insert " + e.getMessage());
                    Toast.makeText(MainActivity2.this, "Invalid text", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setRef() {

        et_prodName = findViewById(R.id.et_prodName);
        et_prodDesc = findViewById(R.id.et_prodDesc);
        et_prodPrice = findViewById(R.id.et_prodPrice);
        et_prodQuantity = findViewById(R.id.et_prodQuantity);
        et_prodStatus = findViewById(R.id.et_prodStatus);
        btn_add = findViewById(R.id.btn_add);
        btn_update = findViewById(R.id.btn_update);
        btn_delete = findViewById(R.id.btn_delete);
        btn_view = findViewById(R.id.btn_view);

        DB = new DBHelper(this);

    }
}
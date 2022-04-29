package com.example.sqlite_crud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class dashboard extends AppCompatActivity {

    private StorageReference userStorage;
    private FirebaseUser user;
    private DatabaseReference userDatabase;
    private StorageTask addTask;
    private Task addTaskNoImage;
    private String userID, tempImageName;

    private ImageView iv_editButton, iv_saveButton, iv_profile_photo, btn_cancel;
    private EditText tv_fname, tv_lname, tv_contactNum, tv_email;
    private TextView tv_uploadPhoto, tv_signout;
    private ProgressBar progressBar;

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users");
        userStorage = FirebaseStorage.getInstance().getReference("Users").child(userID);


        setRef();
        clickListenrs();
        generateProfile();

    }

    private void clickListenrs() {

        iv_editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_fname.setEnabled(true);
                tv_lname.setEnabled(true);
                tv_contactNum.setEnabled(true);
                iv_editButton.setVisibility(View.INVISIBLE);
                iv_saveButton.setVisibility(View.VISIBLE);
                tv_uploadPhoto.setVisibility(View.VISIBLE);
                btn_cancel.setVisibility(View.VISIBLE);
            }
        });

        iv_saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if((addTask != null && addTask.isInProgress()) || (addTaskNoImage != null))
                {
                    Toast.makeText(dashboard.this, "In progress", Toast.LENGTH_SHORT).show();
                } else {

                    ;
                    new AlertDialog.Builder(dashboard.this)
                            .setTitle("Update Project")
                            .setMessage("Please make sure all information entered are correct")
                            .setCancelable(true)
                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if(!hasImage(iv_profile_photo))
                                    {
                                        Toast.makeText(dashboard.this, "With Image", Toast.LENGTH_SHORT);
                                        updateProject();
                                    }
                                    else
                                    {
                                        Toast.makeText(dashboard.this, "No Image", Toast.LENGTH_SHORT).show();
                                        updateProjectNoImage();
                                    }

                                    Toast.makeText(dashboard.this, "Image not detected", Toast.LENGTH_SHORT);


                                }
                            })
                            .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                }
                            })
                            .show();


                    tv_fname.setEnabled(false);
                    tv_lname.setEnabled(false);
                    tv_contactNum.setEnabled(false);
                    iv_editButton.setVisibility(View.VISIBLE);
                    iv_saveButton.setVisibility(View.INVISIBLE);
                    tv_uploadPhoto.setVisibility(View.GONE);
                    btn_cancel.setVisibility(View.INVISIBLE);

                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_fname.setEnabled(false);
                tv_lname.setEnabled(false);
                tv_contactNum.setEnabled(false);
                iv_editButton.setVisibility(View.VISIBLE);
                iv_saveButton.setVisibility(View.INVISIBLE);
                tv_uploadPhoto.setVisibility(View.GONE);
                btn_cancel.setVisibility(View.INVISIBLE);
            }
        });

        tv_uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                boolean pick = true;
                if (pick == true){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else
                        PickImage();

                }else{
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else
                        PickImage();
                }
            }
        });

        tv_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(dashboard.this, login_page.class);
                startActivity(intent);
                Toast.makeText(dashboard.this, "Signed Out", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setRef() {

        iv_editButton = findViewById(R.id.btn_update);
        iv_saveButton = findViewById(R.id.iv_saveButton);
        iv_profile_photo = findViewById(R.id.iv_profile_photo);

        tv_fname = findViewById(R.id.tv_fname);
        tv_lname = findViewById(R.id.tv_lname);
        tv_contactNum = findViewById(R.id.tv_contactNum);
        tv_email = findViewById(R.id.tv_email);
        tv_uploadPhoto = findViewById(R.id.tv_uploadPhoto);
        tv_signout = findViewById(R.id.tv_signout);

        btn_cancel = findViewById(R.id.btn_cancel);

        progressBar = findViewById(R.id.progressBar);


    }

    private void generateProfile() {

        userDatabase.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users userProfile = snapshot.getValue(Users.class);

                if(userProfile != null){
                    String sp_fName = userProfile.firstName;
                    String sp_lName = userProfile.lastName;
                    String sp_num = userProfile.contactNum;
                    String sp_email = userProfile.email;
                    String sp_imageUrl = userProfile.imageUrl;
                    tempImageName = userProfile.imageName;

                    tv_fname.setText(sp_fName);
                    tv_lname.setText(sp_lName);
                    tv_contactNum.setText( sp_num);
                    tv_email.setText(sp_email);

                    Picasso.get().load(sp_imageUrl).into(iv_profile_photo);
                    progressBar.setVisibility(View.GONE);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void PickImage() {
        CropImage.activity().start(this);
    }

    private void updateProject() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile...");
        progressDialog.show();

        StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());

        String sp_fname = tv_fname.getText().toString();
        String sp_lname = tv_lname.getText().toString();
        String sp_contactNum = tv_contactNum.getText().toString();
        String sp_email = tv_email.getText().toString();
        String imageName = imageUri.getLastPathSegment();

        int ratings = 0;


        addTask = fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String imageURL = uri.toString();


                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                        hashMap.put("firstName", sp_fname);
                        hashMap.put("lastName", sp_lname);
                        hashMap.put("contactNum", sp_contactNum);
                        hashMap.put("username", sp_email);
                        hashMap.put("imageName", imageName);
                        hashMap.put("imageUrl", imageURL);


                        userDatabase.child(userID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                StorageReference imageRef = userStorage.child(tempImageName);
                                imageRef.delete();
                                progressDialog.dismiss();
                                Intent intent = new Intent(dashboard.this, dashboard.class);
                                startActivity(intent);
                                Toast.makeText(dashboard.this, "Profile is updated", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(dashboard.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void updateProjectNoImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Profile...");
        progressDialog.show();

        String sp_fname = tv_fname.getText().toString();
        String sp_lname = tv_lname.getText().toString();
        String sp_contactNum = tv_contactNum.getText().toString();
        String sp_email = tv_email.getText().toString();


        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        hashMap.put("firstName", sp_fname);
        hashMap.put("lastName", sp_lname);
        hashMap.put("contactNum", sp_contactNum);
        hashMap.put("username", sp_email);


        addTaskNoImage = userDatabase.child(userID).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                progressDialog.dismiss();
                Intent intent = new Intent(dashboard.this, dashboard.class);
                startActivity(intent);
                Toast.makeText(dashboard.this, "Profile is updated", Toast.LENGTH_SHORT).show();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(dashboard.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private boolean hasImage(ImageView iv){

        Drawable drawable = iv.getDrawable();
        BitmapDrawable bitmapDrawable = drawable instanceof BitmapDrawable ? (BitmapDrawable)drawable : null;

        return bitmapDrawable == null || bitmapDrawable.getBitmap() == null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();

                try{
                    Picasso.get().load(imageUri)
                            .into(iv_profile_photo);

                }catch (Exception e){
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    // validate permissions
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
    }

    private boolean checkStoragePermission() {
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res2;
    }

    private boolean checkCameraPermission() {
        boolean res1 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED;
        boolean res2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        return res1 && res2;
    }
}
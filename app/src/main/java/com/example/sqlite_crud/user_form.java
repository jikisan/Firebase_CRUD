package com.example.sqlite_crud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class user_form extends AppCompatActivity {

    private EditText et_firstName, et_lastName, et_contactNumber, et_username, et_password_signup,
            et_confirmPassword;
    private ImageView iv_profile_photo;
    private TextView tv_uploadPhoto, textView;
    private Button btn_signUp;
    private FirebaseAuth fAuth;
    private FirebaseUser user;

    private StorageReference userStorage;
    private DatabaseReference userDatabase;

    private String userID, category, userKey;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference(Users.class.getSimpleName());
        userStorage = FirebaseStorage.getInstance().getReference("Users").child(userID);
        category = getIntent().getStringExtra("category");
        userKey = getIntent().getStringExtra("user id");


        setRef();
        ClickListener();

        if(category != null)
        {
            if(category.equals("edit"))
            {
                textView.setText("Edit user");
                btn_signUp.setText("Update");
                generateData(userKey);
            }
        }

    }

    private void generateData(String userKey) {
        userDatabase.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Users users = snapshot.getValue(Users.class);

                    String imageUrl = users.imageUrl;
                    String firstName = users.firstName;
                    String lastName = users.lastName;
                    String contactNum = users.contactNum;
                    String email = users.email;

                    Picasso.get()
                            .load(imageUrl)
                            .into(iv_profile_photo);

                    et_firstName.setText(firstName);
                    et_lastName.setText(lastName);
                    et_username.setText(email);
                    et_contactNumber.setText(contactNum);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ClickListener() {

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(category != null) {
                    if (category.equals("edit")) {
                        if (imageUri == null) {
                            updateUserNoImage();
                        } else {
                            updateUser();
                        }

                    }
                }
                else
                {
                    signUpUser();
                }

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

    }

    private void updateUser() {
        {
            String firstName = et_firstName.getText().toString();
            String lastName = et_lastName.getText().toString();
            String username = et_username.getText().toString();
            String password = "";
            String confirmPass = et_confirmPassword.getText().toString();
            String contactNum = et_contactNumber.getText().toString();
            String ratings = "0";

            if (imageUri == null)
            {
                Toast.makeText(this, "Profile photo is required", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(firstName))
            {
                et_firstName.setError("This field is required");
            }
            else if (TextUtils.isEmpty(lastName))
            {
                et_lastName.setError("This field is required");
            }
            else if (TextUtils.isEmpty(contactNum))
            {
                et_contactNumber.setError("This field is required");
            }
            else if (TextUtils.isEmpty(username) )
            {
                et_username.setError("This field is required");
            }
            else if ( !Patterns.EMAIL_ADDRESS.matcher(username).matches())
            {
                et_username.setError("Incorrect Email Format");
            }
            else if (contactNum.length() < 11)
            {
                et_contactNumber.setError("Contact number must be 10 digit");
            }
            else
            {
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Creating account");
                progressDialog.show();

                StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
                String imageName = imageUri.getLastPathSegment();

                fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String imageURL = uri.toString();

                                String uid = "";
                                Users users = new Users(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                                userDatabase.child(userKey).setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();
                                            Intent intent = new Intent(user_form.this, dashboard_two.class);
                                            startActivity(intent);
                                            Toast.makeText(user_form.this, "User Created", Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(user_form.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(user_form.this, "Failed: ", Toast.LENGTH_LONG).show();
                                    }
                                });

                    }
                });


            }
        }
    }

    private void updateUserNoImage() {
        String firstName = et_firstName.getText().toString();
        String lastName = et_lastName.getText().toString();
        String username = et_username.getText().toString();
        String contactNum = et_contactNumber.getText().toString();


        if (TextUtils.isEmpty(firstName))
        {
            et_firstName.setError("This field is required");
        }
        else if (TextUtils.isEmpty(lastName))
        {
            et_lastName.setError("This field is required");
        }
        else if (TextUtils.isEmpty(contactNum))
        {
            et_contactNumber.setError("This field is required");
        }
        else if (TextUtils.isEmpty(username) )
        {
            et_username.setError("This field is required");
        }
        else if ( !Patterns.EMAIL_ADDRESS.matcher(username).matches())
        {
            et_username.setError("Incorrect Email Format");
        }
        else if (contactNum.length() < 11)
        {
            et_contactNumber.setError("Contact number must be 10 digit");
        }
        else
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Creating account");
            progressDialog.show();

            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("firstName", firstName);
            hashMap.put("lastName", lastName);
            hashMap.put("contactNum", contactNum);
            hashMap.put("username", username);

            userDatabase.child(userKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        progressDialog.dismiss();
                        Intent intent = new Intent(user_form.this, dashboard_two.class);
                        startActivity(intent);
                        Toast.makeText(user_form.this, "User Updated", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(user_form.this, "Update Failed ", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void signUpUser()
    {
        String firstName = et_firstName.getText().toString();
        String lastName = et_lastName.getText().toString();
        String username = et_username.getText().toString();
        String password = "";
        String confirmPass = et_confirmPassword.getText().toString();
        String contactNum = et_contactNumber.getText().toString();
        String ratings = "0";

        if (imageUri == null)
        {
            Toast.makeText(this, "Profile photo is required", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(firstName))
        {
            et_firstName.setError("This field is required");
        }
        else if (TextUtils.isEmpty(lastName))
        {
            et_lastName.setError("This field is required");
        }
        else if (TextUtils.isEmpty(contactNum))
        {
            et_contactNumber.setError("This field is required");
        }
        else if (TextUtils.isEmpty(username) )
        {
            et_username.setError("This field is required");
        }
        else if ( !Patterns.EMAIL_ADDRESS.matcher(username).matches())
        {
            et_username.setError("Incorrect Email Format");
        }
        else if (contactNum.length() < 11)
        {
            et_contactNumber.setError("Contact number must be 10 digit");
        }
//        else if (TextUtils.isEmpty(password))
//        {
//            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
//        }
//        else if (password.length() < 8)
//        {
//            Toast.makeText(this, "Password must be 8 or more characters", Toast.LENGTH_LONG).show();
//
//        }
//        else if (!isValidPassword(password))
//        {
//            Toast.makeText(this, "Please choose a stronger password. Try a mix of letters, numbers, and symbols.", Toast.LENGTH_LONG).show();
//        }
//        else if (TextUtils.isEmpty(confirmPass))
//        {
//            Toast.makeText(this, "Please confirm the password", Toast.LENGTH_SHORT).show();
//        }
//        else if (!password.equals(confirmPass))
//        {
//            Toast.makeText(this, "Password did not match", Toast.LENGTH_SHORT).show();
//        }
        else
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Creating account");
            progressDialog.show();

            StorageReference fileReference = userStorage.child(imageUri.getLastPathSegment());
            String imageName = imageUri.getLastPathSegment();

            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String imageURL = uri.toString();

                            String uid = "";
                            Users users = new Users(uid, firstName, lastName, contactNum, username, password, imageName, imageURL, ratings);

                            userDatabase.push().setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {

                                        progressDialog.dismiss();
                                        Intent intent = new Intent(user_form.this, dashboard_two.class);
                                        startActivity(intent);
                                        Toast.makeText(user_form.this, "User Created", Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(user_form.this, "Creation Failed ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(user_form.this, "Failed: ", Toast.LENGTH_LONG).show();
                                }
                            });

                }
            });


        }
    }

    private static boolean isValidPassword(String password) {

        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=?!#$%&()*+,./])"
                + "(?=\\S+$).{8,15}$";


        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);

        return m.matches();
    }

    private void setRef() {

        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_contactNumber = findViewById(R.id.et_contactNumber);
        et_username = findViewById(R.id.et_username);
        et_password_signup = findViewById(R.id.et_password_signup);
        et_confirmPassword = findViewById(R.id.et_confirmPassword);

        btn_signUp = findViewById(R.id.btn_signUp);

        tv_uploadPhoto = findViewById(R.id.tv_uploadPhoto);
        textView = findViewById(R.id.textView);

        iv_profile_photo = findViewById(R.id.iv_profile_photo);

        fAuth = FirebaseAuth.getInstance();

    }

    private void PickImage() {
        CropImage.activity().start(this);
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
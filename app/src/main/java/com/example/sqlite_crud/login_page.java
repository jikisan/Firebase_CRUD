package com.example.sqlite_crud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class login_page extends AppCompatActivity {

    private EditText et_username, et_password;
    private CheckBox checkBox_rememberMe;
    private TextView tv_forgotPassword, tv_signUp;
    private Button btn_login, btn_guest;
    private FirebaseAuth fAuth;
    private DatabaseReference userDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        setRef();


        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), dashboard.class));
            finish();
        }

        ClickListener();


    }

    private void ClickListener() {



        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSignUp = new Intent(login_page.this, sign_up.class);
                startActivity(intentSignUp);

            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_username.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    et_username.setError("Email is Required");
                    return;
                }
                else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    et_username.setError("Incorrect Email Format");
                }
                else if (TextUtils.isEmpty(password)) {
                    et_password.setError("Password is Required");
                    return;
                }

                else if (password.length() < 8) {
                    et_password.setError("Password must be 8 or more characters");
                    return;
                }
                else if (!isValidPassword(password))
                {
                    Toast.makeText(login_page.this, "Passwords should contain atleast one: uppercase letters: A-Z." +
                            " One lowercase letters: a-z. One number: 0-9. ", Toast.LENGTH_LONG).show();
                }
                else
                {
                    fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if(user.isEmailVerified()){
                                    Toast.makeText(login_page.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), dashboard.class));
                                }else{
                                    user.sendEmailVerification();
                                    Toast.makeText(login_page.this, "Please check your email to verify your account.", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(login_page.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }


            }
        });

        tv_forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());


                AlertDialog.Builder pwResetDialog = new AlertDialog.Builder(view.getContext());
                pwResetDialog.setTitle("Reset Password?");
                pwResetDialog.setMessage("Please enter your email to reset password.");
                pwResetDialog.setView(resetMail);

                pwResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String email = resetMail.getText().toString();

                        if (TextUtils.isEmpty(email))
                        {
                            et_username.setError("Email is Required");
                            return;
                        }
                        else if ( !Patterns.EMAIL_ADDRESS.matcher(email).matches())
                        {
                            et_username.setError("Incorrect Email Format");
                        }
                        else
                        {

                            fAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    Toast.makeText(login_page.this, "Please check your email to reset your password.", Toast.LENGTH_SHORT).show();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(login_page.this, "Error!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }


                    }
                });

                pwResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                pwResetDialog.create().show();
            }
        });

    }

    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=?!])"
                + "(?=\\S+$).{8,15}$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);

        return m.matches();
    }

    private void rememberLoginChecker() {
        if (checkBox_rememberMe.isChecked()) {
            if (fAuth.getCurrentUser() != null) {
                startActivity(new Intent(getApplicationContext(), homepage.class));
                finish();
            }
        }
    }

    private void setRef() {

        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        checkBox_rememberMe = findViewById(R.id.cb_rememberMe);
        tv_forgotPassword = findViewById(R.id.tv_ForgotPassword);
        tv_signUp = findViewById(R.id.tv_signUp);
        btn_login = findViewById(R.id.btn_login);
        fAuth = FirebaseAuth.getInstance();


    }
}
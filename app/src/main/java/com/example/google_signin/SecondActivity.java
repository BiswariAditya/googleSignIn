package com.example.google_signin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SecondActivity extends AppCompatActivity {

    TextView email, name, contact, nameMain, emailMain, nameDialog, role;
    ImageView editButton, profileImage;
    Button signOutButton, button;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    AlertDialog editDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        showWelcomeDialog();

        profileImage = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        nameMain = findViewById(R.id.name_main);
        contact = findViewById(R.id.contact);
        emailMain = findViewById(R.id.email_main);
        editButton = findViewById(R.id.edit_btn);
        signOutButton = findViewById(R.id.sign_out);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        role = findViewById(R.id.role);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            String personName = account.getDisplayName();
            String personEmail = account.getEmail();
            nameMain.setText(personName);
            name.setText(personName);
            email.setText(personEmail);
            emailMain.setText(personEmail);
            if (account.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(account.getPhotoUrl())
                        .into(profileImage);
            }
        }
        Intent intent = getIntent();
        if (intent!=null){
            String userPhone = intent.getStringExtra("userPhone");
            String userRole = intent.getStringExtra("userRole");
            contact.setText(userPhone);
            role.setText(userRole);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = getLayoutInflater().inflate(R.layout.profile_edit_dialog, null);
        EditText ename, econtact, erole;
        ename = view.findViewById(R.id.name_edit);
        econtact = view.findViewById(R.id.phone_edit);
        erole = view.findViewById(R.id.role_edit);
        Button update = view.findViewById(R.id.update_button);

        // Inside the update button click listener
        update.setOnClickListener(v -> {
            String newName = ename.getText().toString();
            String newContact = econtact.getText().toString();
            String newRole = erole.getText().toString();

            // Update the UI immediately
            name.setText(newName);
            nameMain.setText(newName);
            contact.setText(newContact);
            role.setText(newRole);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

                userRef.child("name").setValue(newName);
                userRef.child("phone").setValue(newContact);
                userRef.child("role").setValue(newRole)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SecondActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SecondActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SecondActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }

            editDialog.dismiss();
        });


        builder.setView(view);
        editDialog = builder.create();

        editButton.setOnClickListener(v -> editDialog.show());

        signOutButton.setOnClickListener(v -> signOut());
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SecondActivity.this, "Sign out successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SecondActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SecondActivity.this, "Sign out failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showWelcomeDialog() {
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_box);

        nameDialog = dialog.findViewById(R.id.name_dialog);

        String userName = getIntent().getStringExtra("userName");

        nameDialog.setText(userName);

        button = dialog.findViewById(R.id.button);

        button.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
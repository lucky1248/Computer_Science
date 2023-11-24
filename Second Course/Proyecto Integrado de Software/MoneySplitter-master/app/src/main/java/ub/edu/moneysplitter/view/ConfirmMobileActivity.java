package ub.edu.moneysplitter.view;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import ub.edu.moneysplitter.R;

public class ConfirmMobileActivity extends AppCompatActivity {

    private Button mConfirmButton;
    private EditText mCodeVerif;
    private FirebaseAuth mAuth;
    private String TLF;
    private String prefTLF;
    private String verifID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mobile);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Confirmar Teléfono");

        mAuth = FirebaseAuth.getInstance();

        mConfirmButton = findViewById(R.id.buttonConfirmar);
        mCodeVerif = findViewById(R.id.editTextVerificacio);
        mConfirmButton.setOnClickListener(view -> {
            //Intent intent = new Intent(ConfirmMobileActivity.this, HomeActivity.class);
            //startActivity(intent);
            returnReply();
        });

        Intent intent = getIntent();
        TLF = intent.getStringExtra("TLF");
        prefTLF = intent.getStringExtra("prefTLF");
        verifID  = intent.getStringExtra("verifID");

        mConfirmButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
    }



    public void returnReply() {
                Intent replyIntent = new Intent();
        replyIntent.putExtra("verifID", verifID);
        String code = mCodeVerif.getText().toString();
        replyIntent.putExtra("code", code);
        setResult(RESULT_OK,replyIntent);
        finish();
    }


}
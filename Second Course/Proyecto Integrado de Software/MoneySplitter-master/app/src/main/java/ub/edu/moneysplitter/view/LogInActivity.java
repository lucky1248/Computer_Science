package ub.edu.moneysplitter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.SaveSharedPreference;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class LogInActivity extends AppCompatActivity {

    private Button mLoginButton;
    private final String TAG = "LogInActivity";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private EditText mIDText;
    private EditText mPasswordText;
    private UserViewModel mUserViewModel;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Login");


        mAuth = FirebaseAuth.getInstance();

        mUserViewModel = new ViewModelProvider(this)
                .get(UserViewModel.class);





        mLoginButton = findViewById(R.id.buttonAcceder);
        mIDText = findViewById(R.id.editTextID);
        mPasswordText = findViewById(R.id.editTextMiembros);
        mProgressBar = findViewById(R.id.progress_loader);

        mLoginButton.setOnClickListener(view -> {
            //Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
            //startActivity(intent);
            ((TextView) findViewById(R.id.textID)).setTextColor(Color.parseColor("#000000"));
            mIDText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text));
            ((TextView) findViewById(R.id.textPSWD)).setTextColor(Color.parseColor("#000000"));
            mPasswordText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text));

            if(mIDText.getText().toString().equals("")){
                ((TextView) findViewById(R.id.textID)).setTextColor(Color.parseColor("#FF0000"));
                mIDText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text_error));

            }
            else if(mPasswordText.getText().toString().equals("")) {
                ((TextView) findViewById(R.id.textPSWD)).setTextColor(Color.parseColor("#FF0000"));
                mPasswordText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text_error));


            } else {
                mLoginButton.setTextScaleX(0);
                mProgressBar.setVisibility(View.VISIBLE);
                logIn(mIDText.getText().toString(), mPasswordText.getText().toString());
            }

        });

        mLoginButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
    }

    public void logIn(String id, String pswd) {

            mAuth.signInWithEmailAndPassword(id + "@moneysplitter.com", pswd)
                    .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) { // Si es pot loguejar, passa a la Home
                                if(!SaveSharedPreference.getLoggedStatus(LogInActivity.this)){
                                    SaveSharedPreference.setLoggedIn(LogInActivity.this, true, id, pswd);
                                }

                                Intent intent = new Intent(LogInActivity.this, HomeActivity.class);
                                intent.putExtra("userID",id);
                                addUserToken(id);
                                startActivity(intent);
                                finish();
                            } else {
                                // Si falla el logueig, fes un Toast
                                Toast.makeText(getApplicationContext(), "EL Teléfono o la contraseña son erroneos",
                                        Toast.LENGTH_SHORT).show();

                                ((TextView) findViewById(R.id.textID)).setTextColor(Color.parseColor("#FF0000"));
                                mIDText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text_error));
                                ((TextView) findViewById(R.id.textPSWD)).setTextColor(Color.parseColor("#FF0000"));
                                mPasswordText.setBackground(ContextCompat.getDrawable(LogInActivity.this, R.drawable.rounded_corners_plain_text_error));
                                mLoginButton.setTextScaleX(1);
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


    }



    private void addUserToken(String userID){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        mUserViewModel.addUserToken(token, userID);
                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        //Log.d(TAG, msg);
                        //Toast.makeText(LogInActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
package ub.edu.moneysplitter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import ub.edu.moneysplitter.R;

import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class SignUpActivity extends AppCompatActivity {
    private static final int TEXT_REQUEST = 1;
    private final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    private UserViewModel mUserViewModel;
    private Button mSignUpButton;
    private EditText mName;
    private EditText mLastName;
    private EditText mPrefTLF;
    private EditText mTLF;
    private EditText mMail;
    private EditText mPswd;
    private EditText mRePswd;
    private ProgressBar mProgressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Registrarse");



        mUserViewModel = new ViewModelProvider(this)
                .get(UserViewModel.class);

        //avoid captcha verification
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        mAuth = FirebaseAuth.getInstance();


        // set this to remove reCaptcha web
        

        mSignUpButton = findViewById(R.id.buttonRegistrarse);
        mName = findViewById(R.id.editTextNombre);
        mLastName = findViewById(R.id.editTextApellidos);
        mPrefTLF = findViewById(R.id.editTextPrefijo);
        mTLF = findViewById(R.id.editTextTelefono);
        mMail = findViewById(R.id.editTextCorreo);
        mPswd = findViewById(R.id.editTextMiembros);
        mRePswd = findViewById(R.id.editTextRepetirContraseña);
        mProgressBar = findViewById(R.id.progress_loader);


        mSignUpButton.setOnClickListener(view -> {
            //Intent intent = new Intent(SignUpActivity.this, ConfirmMobileActivity.class);
            //startActivity(intent);
            ((TextView) findViewById(R.id.textNombreGrupo)).setTextColor(Color.parseColor("#000000"));
            mName.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text));
            ((TextView) findViewById(R.id.textPrefijo)).setTextColor(Color.parseColor("#000000"));
            mPrefTLF.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text));
            ((TextView) findViewById(R.id.textTelefono)).setTextColor(Color.parseColor("#000000"));
            mTLF.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text));
            ((TextView) findViewById(R.id.textMiembros)).setTextColor(Color.parseColor("#000000"));
            mPswd.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text));
            ((TextView) findViewById(R.id.textRepetirContraseña)).setTextColor(Color.parseColor("#000000"));
            mRePswd.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text));

            if(mName.getText().toString().equals("")){
                ((TextView) findViewById(R.id.textNombreGrupo)).setTextColor(Color.parseColor("#FF0000"));
                mName.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else if(mPrefTLF.getText().toString().equals("")){
                ((TextView) findViewById(R.id.textPrefijo)).setTextColor(Color.parseColor("#FF0000"));
                mPrefTLF.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else if(mTLF.getText().toString().equals("")){
                ((TextView) findViewById(R.id.textTelefono)).setTextColor(Color.parseColor("#FF0000"));
                mTLF.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else if(!pswdValid(mPswd.getText().toString())){
                ((TextView) findViewById(R.id.textMiembros)).setTextColor(Color.parseColor("#FF0000"));
                mPswd.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else if(!mPswd.getText().toString().equals(mRePswd.getText().toString())){
                ((TextView) findViewById(R.id.textRepetirContraseña)).setTextColor(Color.parseColor("#FF0000"));
                mRePswd.setBackground(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.rounded_corners_plain_text_error));
            } else {
                mSignUpButton.setTextScaleX(0);
                mProgressBar.setVisibility(View.VISIBLE);
                signUpCheckPhone(mName.getText().toString(), mLastName.getText().toString(), mPrefTLF.getText().toString(), mTLF.getText().toString(), mMail.getText().toString(), mPswd.getText().toString(), mRePswd.getText().toString());
            }
        });

        mSignUpButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));

    }

    private boolean pswdValid(String pswd) {
        if(pswd.length()<8)
            return false;
        boolean may = false;
        boolean min = false;
        boolean number  = false;
        for (int i  = 0; i<pswd.length(); i++){
            Character c = pswd.charAt(i);
            if(Character.isUpperCase(c))
                may = true;
            if(Character.isLowerCase(c))
                min = true;
            if(Character.isDigit(c))
                number = true;
        }
        return min && may && number;
    }

    private void signUpCheckPhone(String name, String lastName, String prefTLF, String TLF, String mail, String pswd, String rePswd) {
        Log.d(TAG, "Button clicked!");
        if(!pswd.equals(rePswd)){
            Context context = getApplicationContext();
            CharSequence text = "La contraseña repetida tiene que coincidir";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        } else {

            PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:" + credential);


                    signUp(credential, name, lastName, prefTLF, TLF, mail, pswd, rePswd);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Context context = getApplicationContext();
                        CharSequence text = "Firebase Error: Credentials invalides";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Context context = getApplicationContext();
                        CharSequence text = "Limite de solicitudes sobrepasado";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    } else {
                        Context context = getApplicationContext();
                        CharSequence text = "ERROR DESCONOCIDO";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                    // Show a message and update the UI
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:" + verificationId);

                    // Save verification ID and resending token so we can use them later
                    Intent intent = new Intent(SignUpActivity.this, ConfirmMobileActivity.class);
                    intent.putExtra("verifID", verificationId);
                    intent.putExtra("TLF", TLF);
                    startActivityForResult(intent, TEXT_REQUEST);

                }
            };


            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(prefTLF+TLF)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(this)                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);


        }

    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TEXT_REQUEST) {
            if (resultCode == RESULT_OK) {
                String verificationId = data.getStringExtra("verifID");
                String code = data.getStringExtra("code");
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signUp(credential, mName.getText().toString(), mLastName.getText().toString(), mPrefTLF.getText().toString(), mTLF.getText().toString(), mMail.getText().toString(), mPswd.getText().toString(), mRePswd.getText().toString());
            }
        }
    }



    private void signUp(PhoneAuthCredential credential, String name, String lastName, String prefTLF, String TLF, String mail, String pswd, String rePswd) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            //FirebaseUser user = task.getResult().getUser();
                            createUser(name, lastName, prefTLF, TLF, mail, pswd, rePswd);
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Context context = getApplicationContext();
                                CharSequence text = "El código de verificación introducido es erroneo";
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
                            }
                        }
                    }
                });

    }

    private void createUser(String name, String lastName, String prefTLF, String tlf, String mail, String pswd, String rePswd) {
        mAuth.createUserWithEmailAndPassword(tlf+"@moneysplitter.com", pswd)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mUserViewModel.addUser(
                                    mail,
                                    name,
                                    lastName,
                                    pswd,
                                    tlf,
                                    prefTLF
                            );
                            // Anar a la pantalla home de l'usuari autenticat
                            Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "Sign up create user not succeeded");
                            Context context = getApplicationContext();
                            CharSequence text = "Ha habido un error inesperado inténtalo de nuevo mas tarde";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

}
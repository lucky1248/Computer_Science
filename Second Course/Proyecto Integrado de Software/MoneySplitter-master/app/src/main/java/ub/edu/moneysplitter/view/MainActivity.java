package ub.edu.moneysplitter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.model.SaveSharedPreference;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.model.UserRepository;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_READ_CONTACTS = 1;
    private static final int REQUEST_INTERNET = 4;
    private static final int REQUEST_ACCESS_NETWORK = 5;
    private Button mLoginButton;
    private Button mSignupButton;
    private Button mTerminosButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }

        if(SaveSharedPreference.getLoggedStatus(MainActivity.this)){

            FirebaseAuth.getInstance().signInWithEmailAndPassword(SaveSharedPreference.getPswd(MainActivity.this) + "@moneysplitter.com", SaveSharedPreference.getUser(MainActivity.this));
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("userID",SaveSharedPreference.getUser(MainActivity.this));
            intent.putExtra("isOldLogged", "true");
            startActivity(intent);
            finish();
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue))); //posar en meter el icono centrado aqui pero hasta que no se tenga sin fondo na

        mLoginButton = findViewById(R.id.loginButton);
        mSignupButton = findViewById(R.id.registerButton);
        mTerminosButton = findViewById(R.id.terminosButton);

        mLoginButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
        });

        mSignupButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        REQUEST_INTERNET);
            } else {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }

        });

        mTerminosButton.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ub.edu/portals/monUB/"));
            startActivity(browserIntent);
        });

        //Colors definition
        mTerminosButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.dark_blue));
        mLoginButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
        mSignupButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));




        //loadContacts();

    }

    public void loadContacts(){


        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(Math.abs(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)));
                String name = cur.getString(Math.abs(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME)));

                if (cur.getInt(Math.abs(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(Math.abs(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        UserContactsLocal.getInstance().addContactUser(name, formatPhoneNum(phoneNo));
                        //Log.i(TAG, "Name: " + name);
                        //Log.i(TAG, "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso, podemos proceder a leer los contactos
                loadContacts();
            } else {
                // El usuario ha denegado el permiso, podemos mostrar un mensaje o desactivar la funcionalidad relacionada

            }
        } else if (requestCode == REQUEST_INTERNET) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso, podemos proceder a leer los contactos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                            REQUEST_ACCESS_NETWORK);
                } else {
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent);
                }

            } else {
                // El usuario ha denegado el permiso, podemos mostrar un mensaje o desactivar la funcionalidad relacionada

            }
        } else if (requestCode == REQUEST_ACCESS_NETWORK) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // El usuario ha concedido el permiso, podemos proceder a leer los contactos
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);

            } else {
                // El usuario ha denegado el permiso, podemos mostrar un mensaje o desactivar la funcionalidad relacionada

            }
        }
    }

    public String formatPhoneNum(String num){

        ArrayList<Character> allowed = new ArrayList<Character>(Arrays.asList('1', '2', '3', '4', '5', '6', '7', '8', '9', '0'));
        String formatedNum = "";
        for(int i = 0; i < num.length(); i++){
            if(allowed.contains(num.charAt(i)))
                formatedNum+=num.charAt(i);
        }

        if(formatedNum.length()>8)
            formatedNum = formatedNum.substring(formatedNum.length()-9);

        return formatedNum;
    }




}
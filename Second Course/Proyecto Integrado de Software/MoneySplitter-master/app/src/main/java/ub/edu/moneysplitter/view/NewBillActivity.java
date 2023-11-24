package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import ub.edu.moneysplitter.BuildConfig;
import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.BillViewModel;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class NewBillActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 3;
    private Group mGroup;
    private Uri uri;
    public Uri getUri() {
        return uri;
    }
    private String concepto;
    private String payerID;
    private BillViewModel mBillViewModel;
    private String mUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bill);



        Intent i = getIntent();
        mGroup = (Group) i.getSerializableExtra("group");
        mUserID = i.getStringExtra("userID");

        mBillViewModel = new ViewModelProvider(this)
                .get(BillViewModel.class);

        //solucion fea
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Nuevo Gasto");
    }


    public void changeFragmentUsers(){


            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
                    .replace(R.id.fragmentContainerView2,NewBillUsersFragment.class,null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();

    }

    public void changeFragmentPersonalized(){


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_out_right, android.R.anim.slide_in_left)
                .replace(R.id.fragmentContainerView2,NewBillPersonalizedFragment.class,null)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

    }


    public Group getgroup() {

        return mGroup;
    }


    public void newBillDataUpdate(Uri uri, String concepto, String payerID){

        this.uri = uri;
        this.concepto = concepto;
        this.payerID = payerID;
    }
    public void createBill(Map<String, String> billUsers) {
        mBillViewModel.createBill(billUsers, concepto, uri, payerID, mUserID, mGroup.getID(), mGroup.getName(), new BillViewModel.CustomCallBack() {
            @Override
            public void onComplete(int success) {
                if (success == -2) {
                    // Handle unsuccessful uploads
                    Toast.makeText(getApplicationContext(), "No se ha podido guardar la imagen", Toast.LENGTH_SHORT).show();
                } else if (success == 1) {
                    finish();
                }
            }
        });
    }

    public void createBillUsers(Map<String, Boolean> billUsers, String total) {
        mBillViewModel.createBillUsers(billUsers, concepto, uri, payerID, mUserID,mGroup.getID(), mGroup.getName(), total, new BillViewModel.CustomCallBack() {
            @Override
            public void onComplete(int success) {
                if (success == -2) {
                    // Handle unsuccessful uploads
                    Toast.makeText(getApplicationContext(), "No se ha podido guardar la imagen", Toast.LENGTH_SHORT).show();
                } else if (success == 1) {
                    finish();
                }
            }
        });
    }
}
package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.User;

public class ProfileActivity extends AppCompatActivity {

    User mUser;
    private ImageButton mHome;
    private TextView mNombre;
    private TextView mId;
    private Button mEditProfile;
    private TextView mCorreu;
    private TextView mTLF;
    private Button mGastos;
    private Button mPagos;
    private Button mApoyar;

    private Button mBillsHistoral;

    private Button mPaymentsHistoral;
    private ImageView mImage;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Perfil");




        mHome = findViewById(R.id.imageButtonHome);
        mEditProfile = findViewById(R.id.editProfileButton);
        mUser = (User) getIntent().getSerializableExtra("mUser");
        mNombre = findViewById(R.id.nomText);
        mNombre.setText(mUser.getFirstName().toString() + " " +mUser.getLastName().toString());
        mId = findViewById(R.id.idText);
        mId.setText(mUser.getId().toString());
        mImage = findViewById(R.id.imageView);
        paintImageView();

        mHome.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);

            intent.putExtra("userID", mUser.getId());
            startActivity(intent);
            finish();

        });
        mEditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            intent.putExtra("mUser", mUser);
            startActivityForResult(intent, 1);
        });

        //Colors definition
        mEditProfile.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));

        mCorreu = findViewById(R.id.correu);
        mCorreu.setText(mUser.getMail());
        mTLF = findViewById(R.id.telefon);
        mTLF.setText(mUser.getTLF());
        mPagos = findViewById(R.id.buttonPagos);
        mPagos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mGastos = findViewById(R.id.buttonGastos);
        mGastos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mApoyar = findViewById(R.id.buttonApoyar);
        mApoyar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mApoyar.getText().toString()));
                startActivity(browserIntent);
            }
        });

        mBillsHistoral = findViewById(R.id.buttonGastos);
        mPaymentsHistoral = findViewById(R.id.buttonPagos);

        mBillsHistoral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ExpensesActivity.class);
                intent.putExtra("userID", mUser.getId());
                startActivity(intent);
                //Intent i = new Intent(ProfileActivity.this, ExpensesActvity.class);
                //startActivity(i);
            }
        });

        mPaymentsHistoral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, PaymentsActivity.class);
                intent.putExtra("userID", mUser.getId());
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    mUser = (User) data.getSerializableExtra("mUser");
                    paintImageView();
            }
        }
    }
    private void paintImageView(){
        if (mUser.getURL()!=null && !mUser.getURL().equals("")) {
            Picasso.get().load(mUser.getURL()).into(mImage);
        }
    }
}
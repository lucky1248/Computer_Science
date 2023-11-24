package ub.edu.moneysplitter.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.User;

public class ChangePasswordActivity extends AppCompatActivity {

    private User mUser;
    private TextView mPswText;
    private EditText mPsw;
    private TextView mNewPswText;
    private EditText mNewPswd;
    private TextView mRePswText;
    private EditText mRePswd;
    private Button mCambiar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Cambiar Contraseña");
        mUser = (User) getIntent().getSerializableExtra("mUser");
        mPswText = findViewById(R.id.textContraseña);
        mPsw = findViewById(R.id.editTextContraseña);
        mNewPswText = findViewById(R.id.textNewContraseña);
        mNewPswd = findViewById(R.id.editTextNewContraseña);
        mRePswText = findViewById(R.id.textRepetirContraseña);
        mRePswd = findViewById(R.id.editTextRepetirContraseña);
        mCambiar = findViewById(R.id.buttonCambiar);
        mCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPswText.setTextColor(Color.parseColor("#000000"));
                mPsw.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text));
                ((TextView) findViewById(R.id.textInstruccionesContraseña)).setTextColor(Color.parseColor("#000000"));
                mNewPswText.setTextColor(Color.parseColor("#000000"));
                mNewPswd.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text));
                mRePswText.setTextColor(Color.parseColor("#000000"));
                mRePswd.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text));
                if(!mPsw.getText().toString().equals(mUser.getPswd())){
                    mPswText.setTextColor(Color.parseColor("#FF0000"));
                    mPsw.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text_error));
                    CharSequence text = "La contraseña actual no es correcta";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(ChangePasswordActivity.this, text, duration);
                    toast.show();
                }
                else if (!pswdValid(mNewPswd.getText().toString())){
                    ((TextView) findViewById(R.id.textInstruccionesContraseña)).setTextColor(Color.parseColor("#FF0000"));
                    mNewPswText.setTextColor(Color.parseColor("#FF0000"));
                    mNewPswd.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text_error));
                    CharSequence text = "La nueva contraseña no es respeta las normas";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(ChangePasswordActivity.this, text, duration);
                    toast.show();
                }
                else if(!mNewPswd.getText().toString().equals(mRePswd.getText().toString())){
                    mNewPswText.setTextColor(Color.parseColor("#FF0000"));
                    mNewPswd.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text_error));
                    mRePswText.setTextColor(Color.parseColor("#FF0000"));
                    mRePswd.setBackground(ContextCompat.getDrawable(ChangePasswordActivity.this, R.drawable.rounded_corners_plain_text_error));
                    CharSequence text = "La contraseña repetida tiene que coincidir";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(ChangePasswordActivity.this, text, duration);
                    toast.show();
                }
                else{
                    mUser.setPswd(mNewPswd.getText().toString(), new CustomCallBack() {
                        @Override
                        public void onComplete() {
                            Intent intent = new Intent(ChangePasswordActivity.this, EditProfileActivity.class);
                            intent.putExtra("mUser", mUser);
                            startActivity(intent);
                            finish();
                        }
                        public void onFailed(String error){
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

    }
    public interface CustomCallBack{
        public void onComplete();
        public void onFailed(String error);
    }

    private boolean pswdValid(@NonNull String pswd) {
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
}
package ub.edu.moneysplitter.model;

import static android.content.ContentValues.TAG;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

import ub.edu.moneysplitter.view.ChangePasswordActivity;

public class User implements Serializable {
    private String mId; // El tlf
    private String mFirstName;
    private String mLastName;
    private String mTlf;
    private String mMail;
    private String mPictureURL; // Url d'Internet, no la foto en si
    private String mPswd;

    // Constructor
    public User(
            String id,
            String firstName,
            String lastName,
            String tlf,
            String mail,
            String pictureURL,
            String pswd) {
        this.mId = id;
        this.mFirstName = firstName;
        this.mLastName = lastName;
        this.mTlf = tlf;
        this.mMail = mail;
        this.mPictureURL = pictureURL;
        this.mPswd = pswd;
    }

    // Getters
    public String getFirstName () {
        return this.mFirstName;
    }
    public String getId() { return this.mId; }
    public String getLastName () {
        return this.mLastName;
    }
    public String getTLF() {
        return this.mTlf;
    }
    public String getMail() {return this.mMail;}
    public String getURL() { return this.mPictureURL; }
    public String getPswd() {return this.mPswd; }

    // Setters
    public void setFirstName (String firstName) { this.mFirstName = firstName; }
    public void setLastName (String lastName) {
        this.mLastName = lastName;
    }
    public void setTLF(String tlf) {
        this.mTlf = tlf;
    }
    public void setMail(String mail) {
        this.mMail = mail;
    }
    public void setUrl(String pictureUrl) { this.mPictureURL = pictureUrl; }
    public void setPswd(String pswd, ChangePasswordActivity.CustomCallBack ccb) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(getMail(), getPswd());
        user.updatePassword(pswd)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mPswd = pswd;
                            FirebaseFirestore.getInstance().collection("users").document(getId()).update("pswd", mPswd);
                            Log.d(TAG, "User password updated.");
                            ccb.onComplete();
                        }
                        else{
                            ccb.onFailed(task.getException().toString());
                        }
                    }
                });


    }
}
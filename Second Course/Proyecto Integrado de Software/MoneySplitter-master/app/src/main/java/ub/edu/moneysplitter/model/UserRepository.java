package ub.edu.moneysplitter.model;

import android.content.Intent;
import android.media.session.MediaSession;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ub.edu.moneysplitter.view.EditProfileActivity;
import ub.edu.moneysplitter.view.HomeActivity;
import ub.edu.moneysplitter.view.LogInActivity;

public class UserRepository {
    private static final String TAG = "Repository";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;

    /** Autoinstància, pel patró singleton */
    private static final UserRepository mInstance = new UserRepository();

    /** Referència a la Base de Dades */
    private FirebaseFirestore mDb;

    public void updateURL(Uri uri, String userID, EditProfileActivity.CustomCallBack ccb) {
        Map<String, Object> updated = new HashMap<>();
        updated.put("picture_url", uri);
        mDb.collection("users")
                .document(userID)
                .update(updated).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ccb.onComplete();
                        } else {
                            Log.d(TAG, "Sign up completion failed");
                        }
                    }
                });

    }


    public interface OnLoadUsersListener {
        void onLoadUsers(ArrayList<User> users);
    }

    public ArrayList<OnLoadUsersListener> mOnLoadUsersListeners = new ArrayList<>();


    public interface OnLoadUserTokens {
        void onLoadUserTokens();
    }

    public ArrayList<OnLoadUserTokens> mOnLoadUserTokensListeners = new ArrayList<>();

    private UserRepository() {mDb = FirebaseFirestore.getInstance();}

    public static UserRepository getInstance() {return mInstance;}


    public void addOnLoadUsersListener(OnLoadUsersListener listener) {mOnLoadUsersListeners.add(listener);}

    public void addOnLoadUserTokensListeners(OnLoadUserTokens listener) {mOnLoadUserTokensListeners.add(listener);}



    public void addUser(
            String email,
            String firstName,
            String secondName,
            String pswd,
            String TLF,
            String prefTLF
    ) {
        // Obtenir informació personal de l'usuari
        Map<String, Object> signedUpUser = new HashMap<>();
        signedUpUser.put("TLF", TLF);
        signedUpUser.put("first", firstName);
        signedUpUser.put("last", secondName);
        signedUpUser.put("mail", email);
        signedUpUser.put("picture_url", null);
        signedUpUser.put("prefTLF", prefTLF);
        signedUpUser.put("pswd", pswd);

        // Afegir-la a la base de dades
        mDb.collection("users").document(TLF).set(signedUpUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Sign up completion succeeded");
                        } else {
                            Log.d(TAG, "Sign up completion failed");
                        }
                    }
                });

        Map<String, Object> usrG = new HashMap<>();
        usrG.put("grupo", new ArrayList<String>());
        mDb.collection("UserGroups").document(TLF).set(usrG)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Groups Fill completion succeeded");
                        } else {
                            Log.d(TAG, "Sign up completion failed");
                        }
                    }
                });
        setUserNotifications(TLF);
    }

    public void setUserNotifications(String userID){
        Map<String, Object> usrN = new HashMap<>();
        usrN.put("groups", new ArrayList<Map<String, String>>());
        mDb.collection("notifications").document(userID).set(usrN)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Group  Notifications completion succeeded");
                        } else {
                            Log.d(TAG, "Group  Notifications completion failed");
                        }
                    }
                });
        Map<String, Object> usrBN = new HashMap<>();
        usrBN.put("bills", new ArrayList<Map<String, String>>());
        mDb.collection("billNotifications").document(userID).set(usrBN)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Group  Notifications completion succeeded");
                        } else {
                            Log.d(TAG, "Group  Notifications completion failed");
                        }
                    }
                });
        Map<String, Object> usrDN = new HashMap<>();
        usrDN.put("settledDebts", new ArrayList<Map<String, String>>());
        mDb.collection("settledNotifications").document(userID).set(usrDN)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Group  Notifications completion succeeded");
                        } else {
                            Log.d(TAG, "Group  Notifications completion failed");
                        }
                    }
                });
        /*Map<String, Object> usrRN = new HashMap<>();
        usrRN.put("bills", new ArrayList<Map<String, String>>());
        mDb.collection("billNotifications").document(userID).set(usrRN)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Group  Notifications completion succeeded");
                        } else {
                            Log.d(TAG, "Group  Notifications completion failed");
                        }
                    }
                });*/


    }

    public void getUser(Map<String, String> userData, String userID) {

        DocumentReference docRef = mDb.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    userData.put("userID", document.getId());
                    userData.put("first", document.getString("first"));
                    userData.put("last", document.getString("last"));
                    userData.put("TLF", document.getString("TLF"));
                    userData.put("mail", document.getString("mail"));
                    userData.put("picture_url", document.getString("picture_url"));
                    userData.put("pswd", document.getString("pswd"));

                    for (UserRepository.OnLoadUsersListener l: mOnLoadUsersListeners) {
                        l.onLoadUsers(null);
                    }
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    public void addUserToken(String token, String userID) {
        Map<String, Object> updated = new HashMap<>();
        updated.put("token", token);
        mDb.collection("users")
                .document(userID)
                .update(updated);

        //checkout it its ok w listener
    }

    public void getUsersTokens(ArrayList<String> tokens, ArrayList<String> userIDs){

        mDb.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                if(userIDs.contains(document.getId())){

                                    //AVISEM QUE ES POT FILTRAR ELS BILLS SI HEM TROBAT EL NOSTRE
                                    tokens.add((String) document.get("token"));
                                }


                            }
                            for (UserRepository.OnLoadUserTokens l: mOnLoadUserTokensListeners) {
                                l.onLoadUserTokens();
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }




}

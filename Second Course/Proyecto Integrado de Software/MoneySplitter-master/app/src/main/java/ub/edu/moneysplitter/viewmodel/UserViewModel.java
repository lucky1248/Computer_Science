package ub.edu.moneysplitter.viewmodel;


import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.storage.FirebaseStorage;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.model.GroupBillsRepository;
import ub.edu.moneysplitter.model.GroupRepository;
import ub.edu.moneysplitter.model.SaveSharedPreference;
import ub.edu.moneysplitter.model.User;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.model.UserRepository;
import ub.edu.moneysplitter.view.EditProfileActivity;
import ub.edu.moneysplitter.view.HomeActivity;
import ub.edu.moneysplitter.view.LogInActivity;

public class UserViewModel extends AndroidViewModel {


    public void addUserToken(String token, String userID) {
        mUserRepository.addUserToken(token, userID);
    }

    public void updateURL(Uri uri, String userID, EditProfileActivity.CustomCallBack ccb) {
        mUserRepository.updateURL(uri, userID, ccb);
    }



    public interface OnGetUserListener {
        void OnGetUser(User users);
    }

    public ArrayList<OnGetUserListener> mOnGetUserListeners = new ArrayList<>();
    public void addOnGetUserListener(OnGetUserListener listener) {mOnGetUserListeners.add(listener);}



    public void ProfilePhotoUpdated(){

    }


    private final String TAG = "UserViewModel";


    private UserRepository mUserRepository;



    public UserViewModel(Application application) {
        super(application);

        // Instancia els atributs


        mUserRepository = UserRepository.getInstance();


    }

    public User getUser(String userID) {
        Map<String, String> userData = new HashMap<>();




        mUserRepository.addOnLoadUsersListener(new UserRepository.OnLoadUsersListener() {
            @Override
            public void onLoadUsers(ArrayList<User> users) {

                User u = new User(userData.get("userID"),
                        userData.get("first"),
                        userData.get("last"),
                        userData.get("TLF"),
                        userData.get("mail"),
                        userData.get("picture_url"),
                        userData.get("pswd"));

                for (UserViewModel.OnGetUserListener l: mOnGetUserListeners) {

                    l.OnGetUser(u);
                }//hace falta doble lsitener porque sino seria impossible
            }
        });

        mUserRepository.getUser(userData, userID);





        return null;
    }

    public void addUser(String mail, String name, String lastName, String pswd, String tlf, String prefTLF) {
        mUserRepository.addUser(mail, name, lastName, pswd, tlf, prefTLF);
    }


    public static String getContact(String userID){
        return UserContactsLocal.getInstance().getContact(userID);
    }

    public static String getUserID(String contact){
        return UserContactsLocal.getInstance().getUserID(contact);
    }




}
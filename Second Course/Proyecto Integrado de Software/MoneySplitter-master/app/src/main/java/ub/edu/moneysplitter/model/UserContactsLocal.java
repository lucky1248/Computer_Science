package ub.edu.moneysplitter.model;

import java.util.HashMap;
import java.util.Map;

public class UserContactsLocal {

    private User u;

    private String userID;

    private Map<String, String> userContacts;
    private Map<String, String> contactUsers;

    private static UserContactsLocal ucl = new UserContactsLocal();

    public UserContactsLocal(String userID){
        this.userID = userID;
        this.userContacts = new HashMap<>();
        this.contactUsers = new HashMap<>();
    }
    public UserContactsLocal(){
        this.userContacts = new HashMap<>();
        this.contactUsers = new HashMap<>();
    }

    public void addContactUser(String contact, String user){

        userContacts.put(user, contact);
        contactUsers.put(contact, user);
    }

    public String getContact(String userID){
        String ret  = userContacts.get(userID);

        if(ret==null)
            return userID;
        return ret;
    }

    public String getUserID(String contact){
        String ret  = contactUsers.get(contact);
        if(ret==null)
            return contact;
        return ret;
    }

    public void setUserID(String userID){
        this.userID = userID;
        addContactUser("Tú", userID);
    }

    public static UserContactsLocal getInstance(){
        return ucl;
    }


}

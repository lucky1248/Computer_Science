package ub.edu.moneysplitter.model;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class GroupBillsRepository {

    private static final String TAG = "GroupRepository";

    /** Autoinstància, pel patró singleton */
    private static final GroupBillsRepository mInstance = new GroupBillsRepository();

    /** Referència a la Base de Dades */
    private FirebaseFirestore mDb;

    public void deleteBill(String billID) {
        mDb.collection("Bills").document(billID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Object url = task.getResult().get("picture_url");
                if (url != null && !url.toString().equals("")) {
                    StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url.toString());
                    ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "Image_deleted");
                        }
                    });
                } else {
                    Log.d(TAG, "NO IMAGE TO DELETE");
                }
                deleteBillAux(billID);
            }
        });
    }
    public void deleteBillAux(String billID){
        mDb.collection("Bills").document(billID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });


        mDb.collection("billNotifications")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ArrayList<Map<String, String>> g = (ArrayList<Map<String, String>>) document.get("bills");
                                ArrayList<Map<String, String>> updated =  new ArrayList<>();
                                if(g!=null) {
                                    for (Map<String, String> item : g) {
                                        if (!item.get("billID").equals(billID))
                                            updated.add(item);

                                    }
                                }
                                document.getReference().update("bills", updated);

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        mDb.collection("GroupBills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ArrayList<String> g = (ArrayList<String>) document.get("bills");
                                ArrayList<String> updated =  new ArrayList<>();
                                if(g!=null){
                                    for(String item : g){
                                        if(!item.equals(billID))
                                            updated.add(item);

                                    }
                                }
                                document.getReference().update("bills", updated);

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    public interface CustomCallBack {
        void onComplete(int success);
    }
    public void createBill(Map<String, String> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String groupName, GroupBillsRepository.CustomCallBack ccb) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        // gets hour in 24h format


        Map<String, Object> newGroup = new HashMap<>();
        newGroup.put("name", concepto);
        newGroup.put("payerID", payerID);
        newGroup.put("hour", calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        newGroup.put("date", formatter.format(date));
        newGroup.put("picture_url", "");
        newGroup.put("billMembers",billUsers);
        newGroup.put("groupName", groupName);
        newGroup.put("groupID", groupID);
        newGroup.put("settled", false);

        if(billUsers.size()==0 || (billUsers.size()==1 && billUsers.containsKey(payerID))){
            newGroup.put("settled", true);
        }
        //getting total
        int totalPrice = 0;
        for(String p:billUsers.values()){
            if(p!=null)
                totalPrice+=(int) Float.parseFloat(p);
        }
        newGroup.put("price", String.valueOf(totalPrice));
        String billID = concepto+groupID+payerID;  //de momento no puedes haber dos bills con el mismo nombre eso si habria que avisar
        if(uri == null) {
            createBillAux(newGroup, billID, billUsers, concepto, uri, payerID, creatorID, groupID, groupName);
            ccb.onComplete(1);
        }
        else{
            StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(
                    "images/bills/" + billID);
            UploadTask uploadTask = imageRef.putFile(uri);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@android.support.annotation.NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "Error uploading image");
                    ccb.onComplete(-2);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "Image uploaded");
                    getURL(newGroup, billID, billUsers, concepto, uri, payerID, creatorID, groupID, groupName, ccb, imageRef);
                }
            });
        }
    }
    private void getURL(Map<String, Object> newGroup, String billID, Map<String, String> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String groupName, GroupBillsRepository.CustomCallBack ccb, StorageReference imageRef){
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                newGroup.put("picture_url",  uri);
                createBillAux(newGroup, billID, billUsers, concepto, uri, payerID, creatorID, groupID, groupName);
                ccb.onComplete(1);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors

            }
        });
    }

    public void createBillAux(Map<String, Object> newGroup, String billID, Map<String, String> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String groupName) {

        // Afegir-la a la base de dades
        mDb.collection("Bills").document(billID).set(newGroup)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "New group addition completion succeeded");

                        } else {
                            Log.d(TAG, "New group addition completion failed");
                        }
                    }
                });

        DocumentReference docRef = mDb.collection("GroupBills").document(groupID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    //update group object in view too
                    ArrayList<String> g = (ArrayList<String>) document.get("bills");
                    g.add(billID);
                    document.getReference().update("bills", g);

                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });

        addNewBillNoti(new ArrayList<String>(billUsers.keySet())/*(ArrayList<String>)billUsers.keySet()*/, payerID, creatorID,  groupID, groupName, concepto, billID);

    }

    //passem el groupName y no l'objecte per no generar-hi una dependencia
    public void createBill(Map<String, Boolean> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String groupName, String total, GroupBillsRepository.CustomCallBack ccb) {
        Map<String, String> billUsersPrices = new HashMap<>();

        int numMembers = 0;
        for(Boolean cond: billUsers.values()){
            if(cond)
                numMembers++;
        }


        for (String member : billUsers.keySet()) {
            if(billUsers.get(member))
                billUsersPrices.put(member, String.valueOf((Float.parseFloat(total) / ((float) numMembers))));
        }
        if(numMembers>0)
            createBill(billUsersPrices, concepto, uri, payerID,creatorID, groupID, groupName, ccb);
        else
            ccb.onComplete(1);
    }

    private int getItemsNum(Boolean cond, Map<String, Boolean> billUsers){
        int numMembers = 0;
        for(String member:billUsers.keySet()){
            if(billUsers.get(member)==cond){
                numMembers=+1;
            }
        }

        return numMembers;
    }


    //NOTIFICATIONS
    public interface OnFirebaseChangesNewBillsListener {

        public void OnFirebaseChangesNewBills(int numBills);
    }

    public void listenFirebaseChangesNewBills(String userID, GroupBillsRepository.OnFirebaseChangesNewBillsListener onFirebaseChangesNewBillsListener) {
        final DocumentReference docRef = mDb.collection("billNotifications").document(userID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    //snapshot.get("grupo");
                    int numBills = ((ArrayList<Map<String, String>>)snapshot.getData().get("bills")).size();
                    onFirebaseChangesNewBillsListener.OnFirebaseChangesNewBills(numBills);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    /** Definició de listener (interficie),
     *  per escoltar quan s'hagin acabat de llegir els usuaris de la BBDD */
    public interface OnLoadGroupBillsListener {
        void onLoadGroupBills(ArrayList<Bill> users);
    }
    public interface OnLoadGroupBillsIDListener {
        void onLoadGroupBillsID(ArrayList<String> groupBills);
    }

    public ArrayList<GroupBillsRepository.OnLoadGroupBillsListener> mOnLoadGroupBillsListeners = new ArrayList<>();

    public ArrayList<GroupBillsRepository.OnLoadGroupBillsIDListener> mOnLoadGroupBillsIDListeners = new ArrayList<>();


    public interface OnFirebaseChangesListener {
        void OnFirebaseChanges();
    }
    public ArrayList<GroupBillsRepository.OnFirebaseChangesListener> mOnFirebaseChangesListener = new ArrayList<>();
    public void addOnFirebaseChangesListener(GroupBillsRepository.OnFirebaseChangesListener listener) {mOnFirebaseChangesListener.add(listener);}



    public interface OnLoadNewBillsListener {
        void onLoadBills(ArrayList<Bill> bill);
    }
    public ArrayList<GroupBillsRepository.OnLoadNewBillsListener> mOnLoadNewBillsNotiListeners = new ArrayList<>();
    public void addOnLoadNewBillsListener(GroupBillsRepository.OnLoadNewBillsListener listener) {mOnLoadNewBillsNotiListeners.add(listener);}


    public interface OnLoadUserBillsListener {
        void onLoadUserBills(ArrayList<UserBill> users);
    }

    public ArrayList<GroupBillsRepository.OnLoadUserBillsListener> mOnLoadUserBillsListeners = new ArrayList<>();

    public void addOnLoadUserBillsListener(GroupBillsRepository.OnLoadUserBillsListener listener) {mOnLoadUserBillsListeners.add(listener);}


    private GroupBillsRepository() {
        mDb = FirebaseFirestore.getInstance();
    }


    public static GroupBillsRepository getInstance() {
        return mInstance;
    }


    public void addOnLoadGroupBillsListener(GroupBillsRepository.OnLoadGroupBillsListener listener) {
        mOnLoadGroupBillsListeners.add(listener);
    }


    public void addOnLoadGroupBillsIDListener(GroupBillsRepository.OnLoadGroupBillsIDListener listener) {
        mOnLoadGroupBillsIDListeners.add(listener);
    }


    ArrayList<String> groupsBills = new ArrayList<String>();


    public void setGroupBillsbyID(String groupID){

        mDb.collection("GroupBills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());


                                if(document.getId().equals(groupID)){
                                    groupsBills = (ArrayList<String>) document.get("bills");


                                    //AVISEM QUE ES POT FILTRAR ELS BILLS SI HEM TROBAT EL NOSTRE
                                    for (GroupBillsRepository.OnLoadGroupBillsIDListener l: mOnLoadGroupBillsIDListeners) {

                                        l.onLoadGroupBillsID(groupsBills);
                                    }
                                }


                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }



    public void loadGroupBills(ArrayList<Bill> bills){
        bills.clear();

        mDb.collection("Bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());




                                Bill bill = new Bill(
                                        document.getId(), // ID = Email
                                        document.getString("name"),
                                        Float.parseFloat(document.getString("price")),
                                        document.getString("date"),
                                        document.getString("hour"),
                                        document.getString("payerID"),
                                        (Boolean) document.get("settled"),
                                        document.getString("picture_url")
                                );

                                if(groupsBills!=null && groupsBills.contains(document.getId())){

                                    bills.add(bill);
                                }

                            }

                            for (GroupBillsRepository.OnLoadGroupBillsListener l: mOnLoadGroupBillsListeners) {
                                l.onLoadGroupBills(bills);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    //GET BILLS BY USER TO BILLS HISTORIAL
    public interface OnLoadUserFilteredBillsListener{
        public void onLoadUserBills(ArrayList<Bill> bills);
    }

    public void loadUserBills(ArrayList<Bill> bills, String userID, GroupBillsRepository.OnLoadUserFilteredBillsListener l){
        bills.clear();

        mDb.collection("Bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());




                                Bill bill = new Bill(
                                        document.getId(), // ID = Email
                                        document.getString("name"),
                                        Float.parseFloat(document.getString("price")),
                                        document.getString("date"),
                                        document.getString("hour"),
                                        document.getString("payerID"),
                                        (Boolean) document.get("settled"),
                                        document.getString("picture_url")
                                );
                                bill.setGroupID(document.getString("groupName"));
                                Map<String, String> billMembers= (Map<String, String>) document.get("billMembers");

                                if(groupsBills!=null && billMembers.keySet().contains(userID)){

                                    bills.add(bill);
                                }

                            }

                            l.onLoadUserBills(bills);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }






    public void getBillMembersPays(ArrayList<UserBill> userData, String billID) {

        userData.clear();
        DocumentReference docRef = mDb.collection("Bills").document(billID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    Map<String, String> m = (Map<String, String>)document.get("billMembers");

                    if(m!=null){
                        for(String usrID : m.keySet()){
                            userData.add(new UserBill(usrID, billID, Float.parseFloat(m.get(usrID))));
                        }
                    }

                    for (GroupBillsRepository.OnLoadUserBillsListener l: mOnLoadUserBillsListeners) {
                        l.onLoadUserBills(userData);
                    }
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }



    public void deleteNewBillNoti(String billID, String userID) {
        DocumentReference docRef = mDb.collection("billNotifications").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Map<String, String>> data = (ArrayList<Map<String, String>>) document.get("bills");
                    if(data!=null){
                        for (Map<String, String> groupInfo : (ArrayList<Map<String, String>>)data.clone()) {
                            if(groupInfo.get("billID").equals(billID))
                                data.remove(groupInfo);
                        }
                        updateNewBillNoti(userID, data);
                    }




                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    private void addNewBillNoti(ArrayList<String> billMembers, String payerID,  String creatorID, String groupID, String groupName, String billName, String billID){

        mDb.collection("billNotifications")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if((payerID.equals(document.getId())|| billMembers.contains(document.getId())) && !document.getId().equals(creatorID)){
                                    ArrayList<Map<String, String>> g = (ArrayList<Map<String, String>>) document.get("bills");
                                    Map<String, String> groupNoti = new HashMap<>();
                                    groupNoti.put("billID", billID);
                                    groupNoti.put("groupID", groupID);
                                    groupNoti.put("groupName", groupName);
                                    groupNoti.put("billName", billName);
                                    g.add(groupNoti);
                                    //document.getReference().update("grupo", g);
                                    //MAKE IT A DIF METHOD AS UPDATE AT GROUP COLLECTION
                                    //updateGroupMembers(groupID, document.getId());
                                    document.getReference().update("bills", g);
                                }

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    private void updateNewBillNoti(String userID, ArrayList<Map<String, String>> data){
        DocumentReference docRef = mDb.collection("billNotifications").document(userID);
        docRef
                .update("bills", data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public void loadNewBillsNoti(ArrayList<Bill> value, String userID, GroupBillsRepository.OnLoadNewBillsListener l) {
        value.clear();

        DocumentReference docRef = mDb.collection("billNotifications").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Map<String, String>> data = (ArrayList<Map<String, String>>) document.get("bills");
                    if(data!=null){
                        for (Map<String, String> billInfo : data) {
                            Bill b = new Bill(billInfo.get("billID"), billInfo.get("billName"), -1, null, null, null, false, null);
                            b.setGroupID(billInfo.get("groupName")); //hmmm
                            value.add(b);

                        }

                    }


                    /*for (GroupRepository.OnLoadNewGroupsListener l: mOnLoadNewGroupsNotiListeners) {
                        l.onLoadGroups(value);
                    }*/
                    l.onLoadBills(value);
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    GroupBillsRepository.OnFirebaseChangesListener mOnFirebaseBillsChangesListener;



    public void listenFirebaseChanges(String groupID, GroupBillsRepository.OnFirebaseChangesListener mOnFirebaseChangesListener){

        //escucho a las lista de grupos si hay uno nuevo escucho todos los bills del grupo el nuevo included y aviso al listener correspondiente
        //asi si crea un bill se esucha el bill nuevo tmb
        final DocumentReference docRef = mDb.collection("GroupBills").document(groupID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    //snapshot.get("grupo");
                    mOnFirebaseChangesListener.OnFirebaseChanges();
                    listenFirebaseChangesAux(groupID, mOnFirebaseChangesListener);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        //en la primera vez escucho todos los bills actuales
        //listenFirebaseBillChanges(groupID, mOnFirebaseBillsChangesListener);
    }


    public void listenFirebaseChangesAux(String groupID,GroupBillsRepository.OnFirebaseChangesListener mOnFirebaseChangesListener) {

        mDb.collection("GroupBills").document(groupID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            ArrayList<String> billIDs = (ArrayList<String>) document.get("bills");
                            for (String billID : billIDs)
                                listenFirebaseBillChanges(billID, mOnFirebaseChangesListener);
                        }
                    }
                });
    }
    public void listenFirebaseBillChanges(String billID, GroupBillsRepository.OnFirebaseChangesListener mOnFirebaseChangesListener) {



        final DocumentReference docRef = mDb.collection("Bills").document(billID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    //snapshot.get("grupo");
                    mOnFirebaseChangesListener.OnFirebaseChanges();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}


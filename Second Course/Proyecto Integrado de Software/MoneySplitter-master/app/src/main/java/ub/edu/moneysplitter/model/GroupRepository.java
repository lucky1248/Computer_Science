package ub.edu.moneysplitter.model;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.Map;

public class GroupRepository {

    private static final String TAG = "GroupRepository";

    /** Autoinstància, pel patró singleton */
    private static final GroupRepository mInstance = new GroupRepository();

    /** Referència a la Base de Dades */
    private FirebaseFirestore mDb;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private int mSuccess;
    ArrayList<String> userGroups = new ArrayList<String>();


    public void deleteGroup(String groupID) {
        mDb.collection("groups").document(groupID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                deleteGroupAux(groupID);
            }
        });
    }

    public void deleteGroupAux(String groupID) {

        mDb.collection("groups").document(groupID)
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

        mDb.collection("GroupBills").document(groupID)
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
        mDb.collection("notifications")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ArrayList<Map<String, String>> g = (ArrayList<Map<String, String>>) document.get("groups");
                                ArrayList<Map<String, String>> updated =  new ArrayList<>();
                                if(g!=null) {
                                    for (Map<String, String> item : g) {
                                        if (!item.get("groupID").equals(groupID))
                                            updated.add(item);

                                    }
                                }
                                document.getReference().update("groups", updated);

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        mDb.collection("Bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.get("groupID").equals(groupID)){
                                    deleteBill(document.getId(), new GroupRepository.CustomCallBack(){

                                        @Override
                                        public void onComplete(int success) {
                                            document.getReference().delete();
                                        }
                                    });
                                }

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        mDb.collection("UserGroups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ArrayList<String> g = (ArrayList<String>) document.get("grupo");
                                ArrayList<String> updated =  new ArrayList<>();
                                if(g!=null) {
                                    for (String item : g) {
                                        if (!item.equals(groupID))
                                            updated.add(item);

                                    }
                                }
                                document.getReference().update("grupo", updated);

                            }


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });



    }

    public void deleteBill(String billID, CustomCallBack ccb){
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
                ccb.onComplete(0);
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
    }



    public interface CustomCallBack {
        void onComplete(int success);
    }

    public interface DownloadListener {
        void onSuccess();
    }

    public int addGroup(String name, String desc, ArrayList<String> newGroupMembers, String creatorID,
                         Uri pictureUrl, CustomCallBack listener) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();

        Map<String, Object> newGroup = new HashMap<>();
        newGroup.put("name", name);
        newGroup.put("description", desc);
        newGroup.put("date", formatter.format(date));
        newGroup.put("picture_url",  null);
        newGroup.put("members", new ArrayList<String>());

        String groupID = name+creatorID;
        mSuccess = 1;
        groupExists(groupID, new CustomCallBack(){
            @Override
            public void onComplete(int success) {
                mSuccess = success;

                if(success==-1)
                    listener.onComplete(success);
                else
                    addGroupAux(formatter, date, newGroup, groupID, name, desc, newGroupMembers, creatorID, pictureUrl, listener);
            }

        });

        /*if (mSuccess == -1){
            return mSuccess;
        }*/

        /*if(mSuccess == -2){
            return mSuccess;
        }*/
        // Afegir-la a la base de dades

        return mSuccess;
    }

    private void addGroupAux(SimpleDateFormat formatter,Date date,  Map<String, Object> newGroup, String groupID, String name, String desc, ArrayList<String> newGroupMembers, String creatorID,
                             Uri pictureUrl, CustomCallBack listener) {
        if(pictureUrl == null){
            addGroupFirebaseCompletion(formatter, date, newGroup, groupID, name, desc, newGroupMembers, creatorID, listener);
            mSuccess = 1;
            listener.onComplete(mSuccess);
        }
        else {

            StorageReference imageRef = storage.getReference().child(
                    "images/groups/" + groupID);
            UploadTask uploadTask = imageRef.putFile(pictureUrl);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@android.support.annotation.NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d(TAG, "Error uploading image");
                    mSuccess = -2;
                    listener.onComplete(mSuccess);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d(TAG, "Image uploaded");
                    getURL(formatter, date, newGroup, groupID, name, desc, newGroupMembers, creatorID, listener, imageRef);
                }
            });
        }
    }

    private void getURL(SimpleDateFormat formatter,Date date, Map<String, Object> newGroup, String groupID, String name, String desc, ArrayList<String> newGroupMembers,
                        String creatorID, CustomCallBack listener, StorageReference imageRef){
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                newGroup.put("picture_url",  uri);
                addGroupFirebaseCompletion(formatter, date, newGroup, groupID, name, desc, newGroupMembers, creatorID, listener);
                mSuccess = 1;
                listener.onComplete(mSuccess);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors

            }
        });
    }

    private void addGroupFirebaseCompletion(SimpleDateFormat formatter,Date date, Map<String, Object> newGroup, String groupID, String name, String desc, ArrayList<String> newGroupMembers,
                                            String creatorID, CustomCallBack listener) {
            mDb.collection("groups").document(groupID).set(newGroup)
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

            //DO THIS WITH NOTIFIACTIONS
            mDb.collection("notifications")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    if (newGroupMembers.contains(document.getId()) && !document.getId().equals(creatorID)) {
                                        ArrayList<Map<String, String>> g = (ArrayList<Map<String, String>>) document.get("groups");
                                        Map<String, String> groupNoti = new HashMap<>();
                                        groupNoti.put("groupID", groupID);
                                        groupNoti.put("groupName", name);
                                        groupNoti.put("groupDate", formatter.format(date));
                                        g.add(groupNoti);
                                        document.getReference().update("groups", g);
                                        //MAKE IT A DIF METHOD AS UPDATE AT GROUP COLLECTION
                                        //updateGroupMembers(groupID, document.getId());
                                    } else if (document.getId().equals(creatorID)) { //si es el creador no va a les seves notificacions
                                        linkGroupToUser(groupID, creatorID);
                                        //updateGroupMembers(groupID, creatorID);
                                    }

                                }


                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

            //LINK AMB EL GROUPBILLS PER QUE PUGUI TENIR BILLS
            Map<String, Object> data = new HashMap<>();
            data.put("bills", new ArrayList<>());
            mDb.collection("GroupBills").document(groupID).set(data)
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

    }

    public void groupExists(String groupID, CustomCallBack call){
        DocumentReference docRef = mDb.collection("groups").document(groupID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //si existe el grupo, realmente es un fallo, ya que no queremos sobreescribirlo
                        if (call != null) {
                            call.onComplete(-1);
                        }
                    } else {
                        if (call != null) {
                            call.onComplete(1);
                        }
                    }
                }
                else {
                    Log.d(TAG, "Something went worong on trying to find a concrete group");
                }
            }
        });
    }

    public void updateGroupMembers(String groupID, String userID){
        DocumentReference docRef = mDb.collection("groups").document(groupID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        ArrayList<String> l = (ArrayList<String>) document.get("members");
                        l.add(userID);
                        document.getReference().update("members", l);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void loadNewGroupsNoti(ArrayList<Group> value, String userID, GroupRepository.OnLoadNewGroupsListener l) {
        value.clear();
        DocumentReference docRef = mDb.collection("notifications").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Map<String, String>> data = (ArrayList<Map<String, String>>) document.get("groups");
                    if(data!=null){
                        for (Map<String, String> groupInfo : data) {
                        Group g = new Group(groupInfo.get("groupID"), groupInfo.get("groupName"), null, groupInfo.get("groupDate"), null, null);
                        value.add(g);
                    }
                }


                    /*for (GroupRepository.OnLoadNewGroupsListener l: mOnLoadNewGroupsNotiListeners) {
                        l.onLoadGroups(value);
                    }*/
                    l.onLoadGroups(value);
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    public void listenFirebaseChanges(String userID, OnFirebaseChangesListener mOnFirebaseChangesListener){
        final DocumentReference docRef = mDb.collection("UserGroups").document(userID);
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

    public void linkGroupToUser(String groupID, String userID) {
        DocumentReference docRef = mDb.collection("groups").document(groupID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    ArrayList<String> members = (ArrayList<String>) document.get("members");
                    members.add(userID);
                    updateMembers(groupID, members);

                    for (GroupRepository.OnLoadGroupListener l: mOnLoadGroupListeners) {
                        l.OnLoadGroup();
                    }
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });

        docRef = mDb.collection("UserGroups").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    ArrayList<String> groups = (ArrayList<String>) document.get("grupo");
                    groups.add(groupID);
                    updateUserGroups(userID, groups);

                    for (GroupRepository.OnLoadGroupListener l: mOnLoadGroupListeners) {
                        l.OnLoadGroup();
                    }
                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });





    }

    private void updateMembers(String groupID, ArrayList<String> members){
        DocumentReference docRef = mDb.collection("groups").document(groupID);
        docRef
                .update("members", members)
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

    private void updateUserGroups(String userID, ArrayList<String> groups){
        DocumentReference docRef = mDb.collection("UserGroups").document(userID);
        docRef
                .update("grupo", groups)
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

    public void deleteNewGroupNoti(String groupID, String userID) {
        DocumentReference docRef = mDb.collection("notifications").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    ArrayList<Map<String, String>> data = (ArrayList<Map<String, String>>) document.get("groups");
                    if(data!=null){
                        for (Map<String, String> groupInfo : (ArrayList<Map<String, String>>)data.clone()) {
                            if(groupInfo.get("groupID").equals(groupID))
                                data.remove(groupInfo);
                        }
                        updateNewGroupNoti(userID, data);
                    }




                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    private void updateNewGroupNoti(String userID, ArrayList<Map<String, String>> data){
        DocumentReference docRef = mDb.collection("notifications").document(userID);
        docRef
                .update("groups", data)
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

    public interface OnFirebaseChangesNewGroupsListener {

        public void OnFirebaseChangesNewGroups(int numGroups);
    }
    public void listenFirebaseChangesNewGroups(String userID, GroupRepository.OnFirebaseChangesNewGroupsListener onFirebaseChangesNewGroupsListener) {
        final DocumentReference docRef = mDb.collection("notifications").document(userID);
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
                    int numGroups = ((ArrayList<Map<String, String>>)snapshot.getData().get("groups")).size();
                    onFirebaseChangesNewGroupsListener.OnFirebaseChangesNewGroups(numGroups);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    /*public void listenFirebaseChanges(String userID, OnFirebaseChangesListener mOnFirebaseChangesListener){
        final DocumentReference docRef = mDb.collection("UserGroups").document(userID);
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
    }*/


    /** Definició de listener (interficie),
     *  per escoltar quan s'hagin acabat de llegir els usuaris de la BBDD */



    public interface OnLoadGroupListener {
        void OnLoadGroup();
    }

    public ArrayList<GroupRepository.OnLoadGroupListener> mOnLoadGroupListeners = new ArrayList<>();
    public void addOnLoadGroupListener(GroupRepository.OnLoadGroupListener listener) {mOnLoadGroupListeners.add(listener);}


    //LISTENER NOTIFIACIONES DE NEW GROUPS
    public interface OnLoadNewGroupsListener {
        void onLoadGroups(ArrayList<Group> users);
    }
    public ArrayList<GroupRepository.OnLoadNewGroupsListener> mOnLoadNewGroupsNotiListeners = new ArrayList<>();
    public void addOnLoadNewGroupsListener(GroupRepository.OnLoadNewGroupsListener listener) {mOnLoadNewGroupsNotiListeners.add(listener);}


    public interface OnFirebaseChangesListener {
        void OnFirebaseChanges();
    }

    public ArrayList<GroupRepository.OnFirebaseChangesListener> mOnFirebaseChangesListener = new ArrayList<>();
    public void addOnFirebaseChangesListener(GroupRepository.OnFirebaseChangesListener listener) {mOnFirebaseChangesListener.add(listener);}



    public interface OnLoadGroupsListener {
        void onLoadGroups(ArrayList<Group> users);
    }

    public interface OnLoadUserGroupsIDListener {
        void onLoadUserGroupsID(ArrayList<String> userGroups);
    }

    public ArrayList<OnLoadGroupsListener> mOnLoadGroupsListeners = new ArrayList<>();


    public ArrayList<GroupRepository.OnLoadUserGroupsIDListener> mOnLoadUserGroupsIDListeners = new ArrayList<>();

    /** Definició de listener (interficie)
     * per poder escoltar quan s'hagi acabat de llegir la Url de la foto de perfil
     * d'un usuari concret */
    public interface OnLoadGroupPictureUrlListener {
        void OnLoadGroupPictureUrl(String pictureUrl);
    }

    public OnLoadGroupPictureUrlListener mOnLoadUserPictureUrlListener;

    /**
     * Constructor privat per a forçar la instanciació amb getInstance(),
     * com marca el patró de disseny Singleton class
     */
    private GroupRepository() {
        mDb = FirebaseFirestore.getInstance();
    }

    /**
     * Retorna aqusta instancia singleton
     * @return
     */
    public static GroupRepository getInstance() {
        return mInstance;
    }

    /**
     * Afegir un listener de la operació OnLoadUsersListener.
     * Pot haver-n'hi només un. Fem llista, com a exemple, per demostrar la flexibilitat
     * d'aquest disseny.
     * @param listener
     */
    public void addOnLoadUsersListener(OnLoadGroupsListener listener) {
        mOnLoadGroupsListeners.add(listener);
    }
    public void addOnLoadUserGroupsIDListener(GroupRepository.OnLoadUserGroupsIDListener listener) {
        mOnLoadUserGroupsIDListeners.add(listener);
    }

    /**
     * Mètode que llegeix els usuaris. Vindrà cridat des de fora i quan acabi,
     * avisarà sempre als listeners, invocant el seu OnLoadUsers.
     */
    public void loadGroups(ArrayList<Group> groups){
        groups.clear();
        mDb.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Group group = new Group(
                                        document.getId(), // ID = Email
                                        document.getString("name"),
                                        document.getString("description"),
                                        document.getString("date"),
                                        document.getString("picture_url"),
                                        new GroupMembers((ArrayList<String>) document.get("members"))
                                );
                                groups.add(group);
                            }
                            /* Callback listeners */
                            for (OnLoadGroupsListener l: mOnLoadGroupsListeners) {
                                l.onLoadGroups(groups);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }





    public void setUserGroupsbyID(String userID, OnLoadUserGroupsIDListener onLoadUserGroupsIDListener){

        mDb.collection("UserGroups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                if(document.getId().equals(userID)){

                                    userGroups = (ArrayList<String>) document.get("grupo");
                                    //AVISEM QUE ES POT FILTRAR ELS BILLS SI HEM TROBAT EL NOSTRE
                                    /*for (GroupRepository.OnLoadUserGroupsIDListener l: mOnLoadUserGroupsIDListeners) {
                                        l.onLoadUserGroupsID(userGroups);
                                    }*/
                                    onLoadUserGroupsIDListener.onLoadUserGroupsID(userGroups);
                                }


                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void loadUserGroupsbyID(ArrayList<Group> groups, OnLoadGroupsListener l) {
        groups.clear();
        mDb.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Group group = new Group(
                                        document.getId(), // ID = Email
                                        document.getString("name"),
                                        document.getString("description"),
                                        document.getString("date"),
                                        document.getString("picture_url"),
                                        new GroupMembers((ArrayList<String>) document.get("members"))
                                );
                                if(userGroups !=null && userGroups.contains(document.getId())) {

                                    groups.add(group);
                                }
                            }
                            /* Callback listeners */
                            /*for (OnLoadGroupsListener l: mOnLoadGroupsListeners) {
                                l.onLoadGroups(groups);
                            }*/
                            l.onLoadGroups(groups);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }



    /**
     * Mètode que llegeix la Url d'una foto de perfil d'un usuari indicat pel seu
     * email. Vindrà cridat des de fora i quan acabi, avisarà sempre al listener,
     * invocant el seu OnLoadUserPictureUrl.
     */
    /*public void loadPictureOfGroup(String id) {
        mDb.collection("users")
                .document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                mOnLoadUserPictureUrlListener.OnLoadGroupPictureUrl(document.getString("picture_url"));
                            } else {
                                Log.d("LOGGER", "No such document");
                            }
                        } else {
                            Log.d("LOGGER", "get failed with ", task.getException());
                        }
                    }
                });
    }*/



    /**
     * Mètode que guarda la Url d'una foto de perfil que un usuari hagi pujat
     * des de la HomeActivity a la BBDD. Concretament, es cridat pel GroupViewModel.
     * @param
     * @param
     */
    /*public void setPictureUrlOfGroup(String id, String pictureUrl) {
        Map<String, Object> groupEntry = new HashMap<>();
        groupEntry.put("picture_url", pictureUrl);

        mDb.collection("users")
                .document(id)
                .set(groupEntry, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Photo upload succeeded: " + pictureUrl);
                })
                .addOnFailureListener(exception -> {
                    Log.d(TAG, "Photo upload failed: " + pictureUrl);
                });
    }*/



    private void setSuccess(int i) {
        mSuccess = i;
    }

    public void getGroup(Map<String, Object> groupData, String groupID, GroupRepository.OnLoadGroupListener l) {
        /*mDb.collection("GroupBills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if(document.toString().equals(groupID)){
                                    groupsBills = (ArrayList<String>) document.get("bills");
                                }
                                for (GroupBillsRepository.OnLoadGroupBillsIDListener l: mOnLoadGroupBillsIDListeners) {
                                    l.onLoadGroupBillsID(groupsBills);
                                }
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });*/

        DocumentReference docRef = mDb.collection("groups").document(groupID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    groupData.put("groupID", document.getId());
                    groupData.put("name", document.getString("name"));
                    groupData.put("description", document.getString("description"));
                    groupData.put("date", document.getString("date"));
                    groupData.put("picture_url", document.getString("picture_url"));
                    groupData.put("members", document.get("members"));

                    l.OnLoadGroup();

                } else {
                    Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    //LOAD GROUPS HISTORIAL EXPENSES

    public interface OnGroupsExpensesListener {

        public void OnGroupsExpenses(ArrayList<Map<String, String>> groups);
    }

    private void getGroupExpensesAux(String userID, String filter, ArrayList<Map<String, String>> groups, OnGroupsExpensesListener onGroupsExpensesListener){
        mDb.collection("Bills")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            Date currentDate = new Date();
                            Date compareDate;
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(currentDate);


                            //Filtrem per obtenir la data amb la qual compararem
                            if(filter.equals("S"))
                                cal.add(Calendar.DATE, -7);
                            else if(filter.equals("M"))
                                cal.add(Calendar.DATE, -30);
                            else if(filter.equals("A"))
                                cal.add(Calendar.DATE, -365);
                            compareDate = cal.getTime();


                            Map<String, Map<String, String>> groupsData = getIdDataFormat(groups);
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
                                Date bDate;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(Calendar.YEAR, Integer.parseInt(((String)document.get("date")).substring(6, 10)));
                                calendar.set(Calendar.MONTH, Integer.parseInt(((String)document.get("date")).substring(3, 5)));
                                calendar.set(Calendar.DATE, Integer.parseInt(((String)document.get("date")).substring(0, 2)));
                                bDate = calendar.getTime();

                                //la despesa d'un grup es posteriro a la data donada pel limit temporal i es de l'usuari
                                if(((Map<String, String>)document.get("billMembers")).containsKey(userID) && (bDate.after(compareDate) || filter.equals("T"))){

                                    Map<String, String> groupData;
                                    /*if(!groupsData.containsKey(document.get("groupID"))){
                                        groupData= new HashMap<>();
                                        groupData.put("groupID", (String) document.get("groupID"));
                                        groupData.put("groupName", (String) document.get("groupName"));
                                        groupData.put("amount", (String) document.get("price"));
                                    } else {*/
                                        groupData = groupsData.get(document.get("groupID"));
                                        Float newTotal = Float.valueOf(groupData.get("amount"));
                                        newTotal += Float.valueOf((String) document.get("price"));
                                        groupData.put("amount", String.valueOf(newTotal));
                                    //}

                                    groupsData.put((String) document.get("groupID"), groupData);



                                }

                            }

                            groups.addAll(new ArrayList<Map<String, String>>(groupsData.values()));
                            onGroupsExpensesListener.OnGroupsExpenses(new ArrayList<Map<String, String>>(groupsData.values()));

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private Map<String, Map<String, String>> getIdDataFormat(ArrayList<Map<String, String>> groups) {
        Map<String, Map<String, String>> ret = new HashMap<>();
        for(Map<String, String> data: groups){
            ret.put(data.get("groupID"), data);
        }
        return ret;
    }

    public void getGroupsExpenses(String userID, String filter, ArrayList<Map<String, String>> groups, OnGroupsExpensesListener onGroupsExpensesListener){
        groups.clear();
        mDb.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());



                                if(((ArrayList<String>)document.get("members")).contains(userID)){
                                    Map<String, String> groupData = new HashMap<>();
                                    groupData.put("groupID", document.getId());
                                    groupData.put("groupName", (String) document.get("name"));
                                    groupData.put("amount", "0");
                                    groups.add(groupData);



                                }

                            }

                            getGroupExpensesAux(userID, filter, groups, onGroupsExpensesListener);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    public void listenGroupChanges(String groupID, GroupRepository.OnFirebaseChangesListener onFirebaseChangesListener) {
        final DocumentReference docRef = mDb.collection("groups").document(groupID);
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
                    onFirebaseChangesListener.OnFirebaseChanges();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }




}

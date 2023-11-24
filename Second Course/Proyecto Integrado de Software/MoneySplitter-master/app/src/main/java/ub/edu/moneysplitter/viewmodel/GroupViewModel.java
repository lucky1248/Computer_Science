package ub.edu.moneysplitter.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.provider.Settings;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.model.GroupBillsRepository;
import ub.edu.moneysplitter.model.GroupMembers;
import ub.edu.moneysplitter.model.GroupRepository;
import ub.edu.moneysplitter.model.SettleGroupBills;
import ub.edu.moneysplitter.model.User;
import ub.edu.moneysplitter.model.UserRepository;

public class GroupViewModel extends AndroidViewModel {


    public void addListenerFirebaseChanges(String userID) {

        mGroupRepository.listenFirebaseChanges(userID,(new GroupRepository.OnFirebaseChangesListener() {
            @Override
            public void OnFirebaseChanges() {
                GroupViewModel.this.loadGroupsFromRepositoryFiltered(userID);
            }
        }));
    }

    //FIREBASE ACTIVE UPDATES PUT IT OUTSIDE in NOTIFICATIONSVIEWMODEL
    public void addListenerFirebaseNotificationsChanges(String userID, OnFirebaseChangesNewGroupsListener listenerGroups, OnFirebaseChangesNewBillsListener listenerBills, OnFirebaseChangesNewSettleDebtsListener listenerDebts) {

        mGroupRepository.listenFirebaseChangesNewGroups(userID,(new GroupRepository.OnFirebaseChangesNewGroupsListener() {
            @Override
            public void OnFirebaseChangesNewGroups(int numGroups) {
                listenerGroups.OnFirebaseChangesNewGroups(numGroups);
            }
        }));

        mBillRepository.listenFirebaseChangesNewBills(userID,(new GroupBillsRepository.OnFirebaseChangesNewBillsListener() {
            @Override
            public void OnFirebaseChangesNewBills(int numBills) {
                listenerBills.OnFirebaseChangesNewBills(numBills);
            }
        }));

        SettleGroupBills.getInstance().listenFirebaseChangesSettleDebts(userID,(new SettleGroupBills.OnFirebaseChangesNewSettleDebtsListener() {
            @Override
            public void OnFirebaseChangesNewSettleDebts(int numPendingPays, int numRecievedPays) {
                listenerDebts.OnFirebaseChangesNewSettleDebts(numPendingPays, numRecievedPays);
            }
        }));
    }

    public void deleteGroup(String groupID) {
        mGroupRepository.deleteGroup(groupID);
    }

    public interface OnFirebaseChangesNewGroupsListener {

        public void OnFirebaseChangesNewGroups(int numGroups);
    }



    public interface OnFirebaseChangesNewBillsListener {

        public void OnFirebaseChangesNewBills(int numBills);
    }

    public interface OnFirebaseChangesNewSettleDebtsListener {

        public void OnFirebaseChangesNewSettleDebts(int numPendingPays, int numRecievedPays);
    }

    //REST

    public void linkGroupToUser(String groupID, String userID) {
        mGroupRepository.linkGroupToUser(groupID, userID);
    }

    public void deleteNewGroupNoti(String groupID, String userID) {
        mGroupRepository.deleteNewGroupNoti(groupID, userID);
    }

    public interface OnGetGroupListener {
        void OnGetGroup(Group group);
    }

    public ArrayList<OnGetGroupListener> mOnGetGroupListeners = new ArrayList<>();
    public void addOnGetGroupListener(OnGetGroupListener listener) {mOnGetGroupListeners.add(listener);}




    private final String TAG = "GroupViewModel";


    private final MutableLiveData<ArrayList<Group>> mGroups; // Els usuaris que la RecyclerView mostra al hom
    private final MutableLiveData<ArrayList<Group>> mGroupsFull; // Els usuaris que la RecyclerView mostra al home
    //private final MutableLiveData<ArrayList<Bill>> mBills; // Els usuaris que la RecyclerView mostra al home
    private final MutableLiveData<String> mPictureUrl; // URL de la foto de l'usuari logat
    private final MutableLiveData<Integer> mHidPosition;

    /* Repositori (base de dades) dels usuaris */
    private GroupRepository mGroupRepository; // On es manté la informació dels usuaris

    private UserRepository mUserRepository;
    private GroupBillsRepository mBillRepository;// On es manté la informació dels usuaris

    /* Atributs auxiliars */
    private FirebaseStorage mStorage; // Per pujar fitxers grans (fotos) i accedir-hi


    private final MutableLiveData<ArrayList<Group>> mNewGroupsNoti;

    private GroupRepository.OnLoadNewGroupsListener mOnLoadNewGroupsListener;
    private GroupRepository.OnLoadGroupsListener mOnLoadGroupsListener;

    public GroupViewModel(Application application) {
        super(application);

        // Instancia els atributs
        mGroups = new MutableLiveData<>(new ArrayList<>());
        mGroupsFull = new MutableLiveData<>(new ArrayList<>());
        //mBills = new MutableLiveData<>(new ArrayList<>());

        mNewGroupsNoti = new MutableLiveData<>(new ArrayList<>());
        mPictureUrl = new MutableLiveData<>();
        mHidPosition = new MutableLiveData<>();
        mGroupRepository = GroupRepository.getInstance();
        mBillRepository = GroupBillsRepository.getInstance();
        mUserRepository = UserRepository.getInstance();
        mStorage = FirebaseStorage.getInstance();

        // Quan s'acabin de llegir de la BBDD els usuaris, el ViewModel ha d'actualitzar
        // l'observable mUsers. I com que la RecyclerView de la HomeActivity està observant aquesta
        // mateixa variable (mUsers), també se n'enterarà
        /*mGroupRepository.addOnLoadUsersListener(new GroupRepository.OnLoadGroupsListener() {
            @Override
            public void onLoadGroups(ArrayList<Group> users) {
                mGroups.getValue().clear();
                mGroupsFull.getValue().clear();
                GroupViewModel.this.setGroups(users);
                GroupViewModel.this.setGroupsFull((ArrayList<Group>) users.clone());
            }
        });*/


        this.mOnLoadGroupsListener = new GroupRepository.OnLoadGroupsListener() {
            @Override
            public void onLoadGroups(ArrayList<Group> users) {
                GroupViewModel.this.setGroups(users);
                GroupViewModel.this.setGroupsFull((ArrayList<Group>) users.clone());
            }
        };
        this.mOnLoadNewGroupsListener = new GroupRepository.OnLoadNewGroupsListener() {
            @Override
            public void onLoadGroups(ArrayList<Group> users) {
                GroupViewModel.this.setNewGroupsNoti(users);

            }
        };
        /*mGroupRepository.addOnLoadNewGroupsListener(new GroupRepository.OnLoadNewGroupsListener() {
            @Override
            public void onLoadGroups(ArrayList<Group> users) {
                GroupViewModel.this.setNewGroupsNoti(users);

            }
        });*/


    }




    public LiveData<ArrayList<Group>> getGroups() {
        return mGroups;
    }

    public LiveData<ArrayList<Group>> getNewGroupsNoti() {
        return mNewGroupsNoti;
    }



    public LiveData<Integer> getHidPosition() {
        return mHidPosition;
    }


    public void setGroups(ArrayList<Group> groups) {
        mGroups.setValue(new ArrayList<Group>());
        mGroups.setValue(groups);
        ArrayList<String> ids = new ArrayList<>();
        for(Group g:(ArrayList<Group>) ((ArrayList<Group>) mGroups.getValue()).clone()){
            if(ids.contains(g.getID()))
                mGroups.getValue().remove(g);
            else
                ids.add(g.getID());
        }
    }

    public void setGroupsFull(ArrayList<Group> groups) {
        mGroupsFull.setValue(new ArrayList<Group>());
        mGroupsFull.setValue(groups);
        ArrayList<String> ids = new ArrayList<>();
        for(Group g:(ArrayList<Group>) ((ArrayList<Group>) mGroupsFull.getValue()).clone()){

            if(ids.contains(g.getID()))
                mGroupsFull.getValue().remove(g);
            else
                ids.add(g.getID());
        }
    }


    public void setNewGroupsNoti(ArrayList<Group> groups) {
        mNewGroupsNoti.setValue(groups);
    }

    public void loadGroupsFromRepositoryFiltered(String userID) {

        /*mGroupRepository.addOnLoadUserGroupsIDListener(new GroupRepository.OnLoadUserGroupsIDListener() {
            @Override
            public void onLoadUserGroupsID(ArrayList<String> bills) {
                mGroupRepository.loadUserGroupsbyID(mGroups.getValue());
            }
        });*/
        this.mGroups.getValue().clear();
        this.mGroupsFull.getValue().clear();

        mGroupRepository.setUserGroupsbyID(userID, (new GroupRepository.OnLoadUserGroupsIDListener() {
            @Override
            public void onLoadUserGroupsID(ArrayList<String> bills) {
                GroupViewModel.this.mGroups.getValue().clear();
                GroupViewModel.this.mGroupsFull.getValue().clear();
                mGroupRepository.loadUserGroupsbyID(mGroups.getValue(), mOnLoadGroupsListener);
            }
        }));
    }


    public void loadNewGroupsFromRepository(String userID) {


        mGroupRepository.loadNewGroupsNoti(mNewGroupsNoti.getValue(), userID, mOnLoadNewGroupsListener);
    }

    public void loadGroupsFromRepository() {
        mGroupRepository.loadGroups(mGroups.getValue());


    }


    public void removeGroupFromHome(int position) {
        mGroups.getValue().remove(position);
    }



    public Group getGroup(String groupID) {
        Map<String, Object> groupData = new HashMap<>();



        mGroupRepository.addOnLoadGroupListener(new GroupRepository.OnLoadGroupListener() {
            @Override
            public void OnLoadGroup() {

                Group g = new Group((String) groupData.get("groupID"),
                        (String) groupData.get("name"),
                        (String) groupData.get("description"),
                        (String) groupData.get("date"),
                        (String) groupData.get("picture_url"),
                        new GroupMembers((ArrayList<String>) groupData.get("members")));


                for (GroupViewModel.OnGetGroupListener l: mOnGetGroupListeners) {

                    l.OnGetGroup(g);
                }//hace falta doble lsitener porque sino seria impossible
            }
        });

        //mGroupRepository.getGroup(groupData, groupID);


        return null;
    }

    public void filterGroupsBy(String filter){
        ArrayList<Group> groupsFiltered  =new ArrayList<>();
        for(Group g:mGroupsFull.getValue()){
            if(g.getName().toLowerCase().contains(filter)){
                groupsFiltered.add(g);
            }
        }
        mGroups.getValue().clear();
        mGroups.getValue().addAll(groupsFiltered);
        setGroups(mGroups.getValue());
    }


    public interface CustomCallBack {
        void onComplete(int success);
    }

    public int addGroup(String name, String desc, ArrayList<String> newGroupMembers, String creatorID, Uri uri, GroupViewModel.CustomCallBack ccb) {


        return mGroupRepository.addGroup(name, desc, newGroupMembers, creatorID, uri, new GroupRepository.CustomCallBack() {
            @Override
            public void onComplete(int success) {
                ccb.onComplete(success);
            }
        });
    }
}

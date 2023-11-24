package ub.edu.moneysplitter.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Pair;

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
import ub.edu.moneysplitter.model.MemberDebt;
import ub.edu.moneysplitter.model.SettleGroupBills;
import ub.edu.moneysplitter.model.UserBill;
import ub.edu.moneysplitter.view.NewPendingPaysDialogCardAdapter;

public class BillViewModel extends AndroidViewModel {








    private final String TAG = "BillViewModel";

    /* Elements observables del ViewModel */
    private final MutableLiveData<ArrayList<Bill>> mBills; // Els usuaris que la RecyclerView mostra al home
    private final MutableLiveData<ArrayList<UserBill>> mBillMembers; // Els usuaris que la RecyclerView mostra al home
    private final MutableLiveData<String> mPictureUrl; // URL de la foto de l'usuari logat
    private final MutableLiveData<Integer> mHidPosition;

    /* Repositori (base de dades) dels usuaris */
    private GroupBillsRepository mBillRepository; // On es manté la informació dels usuaris

    /* Atributs auxiliars */
    private FirebaseStorage mStorage; // Per pujar fitxers grans (fotos) i accedir-hi


    GroupBillsRepository.OnLoadGroupBillsIDListener ls = new GroupBillsRepository.OnLoadGroupBillsIDListener() {
        @Override
        public void onLoadGroupBillsID(ArrayList<String> bills) {
            mBillRepository.loadGroupBills(mBills.getValue());

        }
    };


    private final MutableLiveData<ArrayList<Bill>> mNewBillsNoti;

    private GroupBillsRepository.OnLoadNewBillsListener mOnLoadNewBillsListener;

    //BALANCE
    private final MutableLiveData<ArrayList<Pair<String, String>>> mBalance;
    private SettleGroupBills.OnGetDebtsListener mBalanceListener;

    //PENDING DEBTS
    private SettleGroupBills.OnSettldDebtsListener pendingDebtsListener;

    private final MutableLiveData<ArrayList<MemberDebt>> mNewPendingPaysNoti;

    private SettleGroupBills.OnSettldDebtsListener recievedDebtsListener;

    private final MutableLiveData<ArrayList<MemberDebt>> mNewRecievedPaysNoti;

    private final SettleGroupBills mDebtsRepository;



    public BillViewModel(Application application) {
        super(application);


        mBills = new MutableLiveData<>(new ArrayList<>());
        mBillMembers = new MutableLiveData<>(new ArrayList<>());
        mBalance = new MutableLiveData<>(new ArrayList<>());
        mNewBillsNoti = new MutableLiveData<>(new ArrayList<>());
        mNewPendingPaysNoti = new MutableLiveData<>(new ArrayList<>());
        mNewRecievedPaysNoti = new MutableLiveData<>(new ArrayList<>());
        mPictureUrl = new MutableLiveData<>();
        mHidPosition = new MutableLiveData<>();
        mBillRepository = GroupBillsRepository.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDebtsRepository = SettleGroupBills.getInstance();


        mBillRepository.addOnLoadGroupBillsListener(new GroupBillsRepository.OnLoadGroupBillsListener() {

            @Override
            public void onLoadGroupBills(ArrayList<Bill> bills) {

                BillViewModel.this.setBills(bills);
            }

        });


        this.mOnLoadNewBillsListener = new GroupBillsRepository.OnLoadNewBillsListener() {
            @Override
            public void onLoadBills(ArrayList<Bill> bills) {
                BillViewModel.this.setNewBillsNoti(bills);
            }
        };
        mBillRepository.addOnLoadGroupBillsIDListener(ls);

        pendingDebtsListener = new SettleGroupBills.OnSettldDebtsListener() {
            @Override
            public void OnSettldDebts(ArrayList<MemberDebt> debts) {

                BillViewModel.this.setNewPendingPaysNoti(debts);
            }
        };

        recievedDebtsListener = new SettleGroupBills.OnSettldDebtsListener() {
            @Override
            public void OnSettldDebts(ArrayList<MemberDebt> debts) {

                BillViewModel.this.setNewRecievedPaysNoti(debts);
            }
        };


        mBalanceListener = new SettleGroupBills.OnGetDebtsListener() {
            @Override
            public void OnGetDebts(Map<String, Float> usersDebt) {
                ArrayList<Pair<String, String>> balance = new ArrayList<>();
                for(String id: usersDebt.keySet())
                    balance.add(new Pair<>(id, String.valueOf(usersDebt.get(id))));


                BillViewModel.this.setBalance(balance);
            }
        };


    }


    public LiveData<Integer> getHidPosition() {
        return mHidPosition;
    }


    //PARTE BILLS

    public LiveData<ArrayList<Bill>> getBills() {
        return mBills;
    }

    public LiveData<ArrayList<UserBill>> getBillMembers() {
        return mBillMembers;
    }


    public void setBills(ArrayList<Bill> bills) {

        mBills.setValue(bills);

        ArrayList<String> ids = new ArrayList<>();
        for(Bill g:(ArrayList<Bill>) ((ArrayList<Bill>) mBills.getValue()).clone()){
            if(ids.contains(g.getID()))
                mBills.getValue().remove(g);
            else
                ids.add(g.getID());
        }
    }










    public void loadBillsFromRepository(String groupID) {

        mBillRepository.setGroupBillsbyID(groupID);


    }



    public interface CustomCallBack {
        void onComplete(int success);
    }

    public void createBill(Map<String, String> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String groupName, BillViewModel.CustomCallBack ccb) {
        mBillRepository.createBill(billUsers, concepto, uri, payerID, creatorID, groupID, groupName, new GroupBillsRepository.CustomCallBack(){

            @Override
            public void onComplete(int success) {
                ccb.onComplete(success);
            }
        });
    }

    public void createBillUsers(Map<String, Boolean> billUsers, String concepto, Uri uri, String payerID, String creatorID, String groupID, String total, String groupName, BillViewModel.CustomCallBack ccb) {
        mBillRepository.createBill(billUsers, concepto, uri, payerID, creatorID, groupID, total, groupName, new GroupBillsRepository.CustomCallBack(){

            @Override
            public void onComplete(int success) {
                ccb.onComplete(success);
            }
        });

    }

    //BALANCE
    public LiveData<ArrayList<Pair<String, String>>> getBalance() {
        return mBalance;
    }
    public void setBalance(ArrayList<Pair<String, String>> balance) {

        mBalance.setValue(balance);

        ArrayList<String> ids = new ArrayList<>();
        for (Pair<String, String> p : (ArrayList<Pair<String, String>>) ((ArrayList<Pair<String, String>>) mBalance.getValue()).clone()) {
            if (ids.contains(p.first))
                mBalance.getValue().remove(p);
            else
                ids.add(p.first);
        }
    }

    public void loadBalanceFromRepository(String groupID, ArrayList<String> groupMembers) {

        mDebtsRepository.getGroupBalance(groupMembers, groupID, mBalanceListener);


    }


    //NOTIFICATIONS

    public void deleteNewBillNoti(String billID, String userID) {
        mBillRepository.deleteNewBillNoti(billID, userID);
    }

    public LiveData<ArrayList<Bill>> getNewBillsNoti() {
        return mNewBillsNoti;
    }

    public void setNewBillsNoti(ArrayList<Bill> bills) {
        mNewBillsNoti.setValue(bills);
    }


    public void loadNewBillsFromRepository(String userID) {
        mBillRepository.loadNewBillsNoti(mNewBillsNoti.getValue(), userID, mOnLoadNewBillsListener);

    }
    public void addListenerFirebaseChanges(String groupID, ArrayList<String> groupMembers) {
        /*mGroupRepository.addOnFirebaseChangesListener(new GroupRepository.OnFirebaseChangesListener() {
            @Override
            public void OnFirebaseChanges() {
                mGroups.getValue().clear();
                mGroupsFull.getValue().clear();
                loadGroupsFromRepositoryFiltered(userID);

            }
        });*/


        mBillRepository.listenFirebaseChanges(groupID,(new GroupBillsRepository.OnFirebaseChangesListener() {
            @Override
            public void OnFirebaseChanges()
            {
                BillViewModel.this.loadBalanceFromRepository(groupID, groupMembers);
                BillViewModel.this.loadBillsFromRepository(groupID);
            }
        }));



    }

    public void deleteBill(String billID) {
        mBillRepository.deleteBill(billID);
    }


    public interface OnSettldDebtsListener {
        void OnSettldDebts(MemberDebt pending, boolean settled);
    }
    public void settleBills(String userID, String groupID,  BillViewModel.OnSettldDebtsListener listener) {
        SettleGroupBills sgb = SettleGroupBills.getInstance();

        sgb.getGroupDebts(groupID, new SettleGroupBills.OnSettldDebtsListener() {
            @Override
            public void OnSettldDebts(ArrayList<MemberDebt> settledData) {

                MemberDebt pending = null;
                for(MemberDebt md : settledData){
                    if(md.getUserID().equals(userID)){
                        pending = md;
                    }


                }
                if(settledData==null || settledData.size()==0){
                    listener.OnSettldDebts(pending, false);
                } else {
                    listener.OnSettldDebts(pending, true);
                }




            }
        });
    }


    //SETTLED DEBTS NOTIFICATIONS
    public void deleteNewPendingPaysNoti(MemberDebt debt) {
        mDebtsRepository.deleteSettledDataNoti(debt);
    }

    public LiveData<ArrayList<MemberDebt>> getNewPendingPaysNoti() {
        return mNewPendingPaysNoti;
    }

    public void setNewPendingPaysNoti(ArrayList<MemberDebt> debts) {
        mNewPendingPaysNoti.setValue(debts);


    }


    public void loadNewPendingPaysFromRepository(String userID) {

        mDebtsRepository.getPendingPaysNoti(userID, this.pendingDebtsListener);
    }



    public LiveData<ArrayList<MemberDebt>> getNewRecievedPaysNoti() {
        return mNewRecievedPaysNoti;
    }

    public void setNewRecievedPaysNoti(ArrayList<MemberDebt> debts) {
        mNewRecievedPaysNoti.setValue(debts);


    }


    public void loadNewRecievedPaysFromRepository(String userID) {

        mDebtsRepository.getRecivedPaysNoti(userID, this.recievedDebtsListener);
    }



    public interface OnFirebaseChangesListener {
        void OnFirebaseChanges(Group group);
    }

    public void listenToCurrentGroup(String groupID, OnFirebaseChangesListener onFirebaseChangesListener) {
        GroupRepository.getInstance().listenGroupChanges(groupID, new GroupRepository.OnFirebaseChangesListener() {
            @Override
            public void OnFirebaseChanges() {
                Map<String, Object> data = new HashMap<>();
                GroupRepository.getInstance().getGroup(data, groupID, new GroupRepository.OnLoadGroupListener() {
                    @Override
                    public void OnLoadGroup() {
                        Group group =  new Group((String) data.get("groupID"), (String) data.get("name"), (String) data.get("description"), (String) data.get("date"), (String) data.get("picture_url"), new GroupMembers((ArrayList<String>) data.get("members")));
                        onFirebaseChangesListener.OnFirebaseChanges(group);
                        BillViewModel.this.loadBalanceFromRepository(groupID, (ArrayList<String>) data.get("members"));
                    }
                });

            }
        });
    }



}

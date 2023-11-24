package ub.edu.moneysplitter.viewmodel;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Map;

import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.GroupBillsRepository;
import ub.edu.moneysplitter.model.GroupRepository;
import ub.edu.moneysplitter.model.SettleGroupBills;

public class HistorialsViewModel extends AndroidViewModel {


    private final MutableLiveData<ArrayList<Map<String, String>>> mPayments;
    private SettleGroupBills.OnGetPayHistorialListener mPaymentsListener;
    private SettleGroupBills settleRepository;

    //HIST BILLS
    private final MutableLiveData<ArrayList<Bill>> mHistBills;
    private GroupBillsRepository.OnLoadUserFilteredBillsListener mHistBillsListener;
    private GroupBillsRepository mBillsRepository;


    //ANALISIS GLOBAL
    private final MutableLiveData<ArrayList<Map<String, String>>> mGroupsByDate;
    private GroupRepository.OnGroupsExpensesListener mGroupByDateListener;
    private GroupRepository mGroupRepository;
    private OnGetExpensesTotalListener mOnGetExpensesTotalListener;

    public HistorialsViewModel(Application application) {
        super(application);
        mPayments = new MutableLiveData<>(new ArrayList<>());
        settleRepository = SettleGroupBills.getInstance();
        mPaymentsListener = new SettleGroupBills.OnGetPayHistorialListener() {
            @Override
            public void OnGetPayHistorial(ArrayList<Map<String, String>> hist) {
                HistorialsViewModel.this.setPayments(hist);
            }
        };

        mHistBills = new MutableLiveData<>(new ArrayList<>());
        mBillsRepository = GroupBillsRepository.getInstance();
        mHistBillsListener = new GroupBillsRepository.OnLoadUserFilteredBillsListener() {
            @Override
            public void onLoadUserBills(ArrayList<Bill> bills) {
                HistorialsViewModel.this.setHistBills(bills);
            }
        };

        mGroupsByDate = new MutableLiveData<>(new ArrayList<>());
        mGroupRepository = GroupRepository.getInstance();
        mGroupByDateListener = new GroupRepository.OnGroupsExpensesListener() {
            @Override
            public void OnGroupsExpenses(ArrayList<Map<String, String>> groups) {
                //CALCULEM EL TOTAL DE DESPESES
                Float totalExpenses = 0f;
                for(Map<String, String> groupD : groups)
                    totalExpenses += Float.valueOf(groupD.get("amount"));

                mOnGetExpensesTotalListener.OnGetExpensesTotal(String.valueOf(totalExpenses));
                //ACTUALITZEM EL MUTABLE LIVE DATA
                HistorialsViewModel.this.setGroupByDate(groups);
            }
        };
    }


    //HISTORIC DE PAGAMENTS
    public LiveData<ArrayList<Map<String, String>>> getPayments() {
        return mPayments;
    }

    private void setPayments(ArrayList<Map<String, String>> hist){
        mPayments.setValue(hist);
    }

    public void loadPaymentsFromRepository(String userID) {
        settleRepository.getPaymentsHistorial(mPayments.getValue(),userID,mPaymentsListener);
    }
    //HISTORIC DE DESPESES

    public LiveData<ArrayList<Bill>> getHistBills() {
        return mHistBills;
    }
    private void setHistBills(ArrayList<Bill> bills) {
        mHistBills.setValue(bills);
    }

    public void loadBillsHistFromRepository(String userID) {
        mBillsRepository.loadUserBills(mHistBills.getValue(),userID,mHistBillsListener);
    }

    //ANALISIS GLOBAL
    public interface OnGetExpensesTotalListener {

        public void OnGetExpensesTotal(String totalExpenses);
    }

    public void setOnGetExpensesTotalListener(OnGetExpensesTotalListener listener){
        this.mOnGetExpensesTotalListener = listener;
    }

    public LiveData<ArrayList<Map<String, String>>> getGroupsByDate() {
        return mGroupsByDate;
    }

    private void setGroupByDate(ArrayList<Map<String, String>> hist){
        mGroupsByDate.setValue(hist);
    }

    public void loadGroupsByDateFromRepository(String userID, String filter) {
        mGroupRepository.getGroupsExpenses(userID, filter, mGroupsByDate.getValue(), mGroupByDateListener);
    }
}

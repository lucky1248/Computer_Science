package ub.edu.moneysplitter.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import ub.edu.moneysplitter.model.GroupBillsRepository;
import ub.edu.moneysplitter.model.UserBill;

public class BillMembersViewModel extends AndroidViewModel {




    private final String TAG = "BillMembersViewModel";

    /* Elements observables del ViewModel */
    private final MutableLiveData<ArrayList<UserBill>> mBillMembers; // Els usuaris que la RecyclerView mostra al home


    /* Repositori (base de dades) dels usuaris */
    private GroupBillsRepository mBillRepository; // On es manté la informació dels usuaris



    public BillMembersViewModel(Application application) {
        super(application);

        // Instancia els atributs
        mBillMembers = new MutableLiveData<>(new ArrayList<>());
        mBillRepository = GroupBillsRepository.getInstance();




        mBillRepository.addOnLoadUserBillsListener(new GroupBillsRepository.OnLoadUserBillsListener() {

            @Override
            public void onLoadUserBills(ArrayList<UserBill> bills) {
                BillMembersViewModel.this.setBillMembers(bills);
            }

        });



    }




    //PARTE BILLS

    public LiveData<ArrayList<UserBill>> getBillMembers() {
        return mBillMembers;
    }

    public void setBillMembers(ArrayList<UserBill> bills) {
        mBillMembers.setValue(bills);
    }





    public void loadBillMemebersFromRepository(String billID) {
        mBillRepository.getBillMembersPays(mBillMembers.getValue(), billID);

    }

}
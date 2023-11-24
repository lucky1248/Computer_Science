package ub.edu.moneysplitter.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.MemberDebt;
import ub.edu.moneysplitter.viewmodel.BillViewModel;

public class NewRecievedPaysDialog extends AppCompatDialogFragment {


    private BillViewModel mBillViewModel;
    private RecyclerView mNewRecievedPaysRV;
    private NewRecievedPaysDialogCardAdapter mNewRecievedPaysRVAdapter;

    View v;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_dialog_bills, null);
        MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded).setView(v)
                .setTitle("Pagos a Recibir")
                .setPositiveButton("Aceptar", (dialog, which) -> {})/*.create()*/;

        this.v = v;



        return madb.create();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate layout with recycler view
        //View v = inflater.inflate(R.layout.fragment_dialog_groups, container, false);
        //mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //setadapter
        //CustomAdapter adapter = new MyRecyclerAdapter(context, customList);
        //mRecyclerView.setAdapter(adapter);
        //get your recycler view and populate it.


        HomeActivity activity = (HomeActivity) getActivity();
        String userID = activity.getUserID();





        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        mBillViewModel = new ViewModelProvider(this)
                .get(BillViewModel.class);//new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mNewRecievedPaysRV = v.findViewById(R.id.userNewBillsDialogRV); //usamos la misma vista que para bills al necesitar las mismas componentes


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mNewRecievedPaysRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mNewRecievedPaysRVAdapter = new NewRecievedPaysDialogCardAdapter(
                mBillViewModel.getNewRecievedPaysNoti().getValue() // Passem-li referencia llista de pagaments pendents
        );

        /*mNewGroupsRVAdapter.setOnClickListener(new NewGroupsDialogCardAdapter.OnClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClick(String groupID, int position, Group group) {


            }
        });*/


        mNewRecievedPaysRVAdapter.setOnClickAcceptListener(new NewRecievedPaysDialogCardAdapter.OnClickAcceptListener() {
            @Override
            public void OnClick(String debtID, int position, MemberDebt md) {

                mNewRecievedPaysRVAdapter.deleteItem(position);
                //check viewmodel delete
                mBillViewModel.deleteNewPendingPaysNoti(md);
            }
        });




        mNewRecievedPaysRV.setAdapter(mNewRecievedPaysRVAdapter); // Associa l'adapter amb la ReciclerView



        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<MemberDebt>> observerBills = new Observer<ArrayList<MemberDebt>>() {
            @Override
            public void onChanged(ArrayList<MemberDebt> users) {

                mNewRecievedPaysRVAdapter.setItemsArrayList(users);
                mNewRecievedPaysRVAdapter.notifyDataSetChanged();
            }
        };
        mBillViewModel.getNewRecievedPaysNoti().observe(getViewLifecycleOwner(), observerBills);




        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mBillViewModel.loadNewRecievedPaysFromRepository(userID);




        return v;
    }
}

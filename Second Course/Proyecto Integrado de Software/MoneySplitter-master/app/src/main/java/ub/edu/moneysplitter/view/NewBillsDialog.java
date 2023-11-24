package ub.edu.moneysplitter.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.BillViewModel;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;

public class NewBillsDialog extends AppCompatDialogFragment {


    private BillViewModel mBillViewModel;
    private RecyclerView mNewBillsRV;
    private NewBillsDialogCardAdapter mNewBillsRVAdapter;

    View v;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_dialog_bills, null);
        MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded).setView(v)
                .setTitle("Gastos nuevos")
                .setPositiveButton("Aceptar", (dialog, which) -> {})/*.create()*/;

        this.v = v;


  // Internament pobla els usuaris de la BBDD

        //AlertDialog.Builder ad =  new AlertDialog.Builder(requireContext());

                /*.setMessage("getString(R.string.order_confirmation)")
                .setPositiveButton("getString(R.string.ok)", (dialog, which) -> {} )
                .create();*/

        //ad

        //return ad.create();
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
        mNewBillsRV = v.findViewById(R.id.userNewBillsDialogRV);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mNewBillsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mNewBillsRVAdapter = new NewBillsDialogCardAdapter(
                mBillViewModel.getNewBillsNoti().getValue() // Passem-li referencia llista usuaris
        );

        /*mNewBillsRVAdapter.setOnClickListener(new NewGroupsDialogCardAdapter.OnClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClick(String groupID, int position, Group group) {


            }
        });*/


        mNewBillsRVAdapter.setOnClickAcceptListener(new NewBillsDialogCardAdapter.OnClickAcceptListener() {
            @Override
            public void OnClick(String billID, int position, Bill bill) {
                mNewBillsRVAdapter.deleteItem(position);
                //check viewmodel delete
                mBillViewModel.deleteNewBillNoti(billID, userID);
            }
        });




        mNewBillsRV.setAdapter(mNewBillsRVAdapter); // Associa l'adapter amb la ReciclerView



        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Bill>> observerBills = new Observer<ArrayList<Bill>>() {
            @Override
            public void onChanged(ArrayList<Bill> users) {
                mNewBillsRVAdapter.notifyDataSetChanged();
            }
        };
        mBillViewModel.getNewBillsNoti().observe(getViewLifecycleOwner(), observerBills);

        /*ItemTouchHelper ith = new ItemTouchHelper(new SwipeToDeleteCallback(mNewBillsRVAdapter));
        ith.attachToRecyclerView(mNewBillsRV);*/

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mBillViewModel.loadNewBillsFromRepository(userID);



        return v;
    }
}

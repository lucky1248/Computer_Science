package ub.edu.moneysplitter.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;

public class NewGroupsDialog extends AppCompatDialogFragment {


    private GroupViewModel mGroupViewModel;
    private RecyclerView mNewGroupsRV;
    private NewGroupsDialogCardAdapter mNewGroupsRVAdapter;

    View v;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_dialog_groups, null);
        MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialAlertDialog_rounded).setView(v)
                .setTitle("Grupos Nuevos")
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
        mGroupViewModel = activity.getViewModel();//new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mNewGroupsRV = v.findViewById(R.id.userNewGroupsDialogRV);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mNewGroupsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mNewGroupsRVAdapter = new NewGroupsDialogCardAdapter(
                mGroupViewModel.getNewGroupsNoti().getValue() // Passem-li referencia llista usuaris
        );

        /*mNewGroupsRVAdapter.setOnClickListener(new NewGroupsDialogCardAdapter.OnClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClick(String groupID, int position, Group group) {


            }
        });*/

        mNewGroupsRVAdapter.setOnClickDeclineListener(new NewGroupsDialogCardAdapter.OnClickDeclineListener() {
            @Override
            public void OnClick(String groupID, int position, Group group) {
                mNewGroupsRVAdapter.deleteItem(position);
                //check viewmodel delete
                mGroupViewModel.deleteNewGroupNoti(groupID, userID);
            }
        });

        mNewGroupsRVAdapter.setOnClickAcceptListener(new NewGroupsDialogCardAdapter.OnClickAcceptListener() {
            @Override
            public void OnClick(String groupID, int position, Group group) {

                mGroupViewModel.linkGroupToUser(groupID, userID);
                mNewGroupsRVAdapter.deleteItem(position);
                //check viewmodel delete
                mGroupViewModel.deleteNewGroupNoti(groupID, userID);
            }
        });




        mNewGroupsRV.setAdapter(mNewGroupsRVAdapter); // Associa l'adapter amb la ReciclerView



        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Group>> observerBills = new Observer<ArrayList<Group>>() {
            @Override
            public void onChanged(ArrayList<Group> users) {
                mNewGroupsRVAdapter.notifyDataSetChanged();
            }
        };
        mGroupViewModel.getNewGroupsNoti().observe(getViewLifecycleOwner(), observerBills);



        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mGroupViewModel.loadNewGroupsFromRepository(userID);



        return v;
    }
}

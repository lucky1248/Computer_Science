package ub.edu.moneysplitter.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.BillViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BillsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BillsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BillViewModel mBillViewModel;


    private Group mGroup;
    private RecyclerView mBillCardsRV; // RecyclerView

    /** Adapter de la RecyclerView */
    private BillCardAdapter mBillCardRVAdapter;

    private FloatingActionButton mAddNewBillButton;

    public BillsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BillsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BillsFragment newInstance(String param1, String param2) {
        BillsFragment fragment = new BillsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }





    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        mAddNewBillButton = view.findViewById(R.id.addNewBillButton);




        GroupActivity activity = (GroupActivity) getActivity();
        String groupID = activity.getgroupID();

        mGroup = activity.getgroup();

        mAddNewBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), NewBillActivity.class);
                i.putExtra("group", activity.getgroup());
                i.putExtra("userID", activity.getuserID());
                startActivity(i);
            }
        });

        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        mBillViewModel = activity.getViewModel();//new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mBillCardsRV = view.findViewById(R.id.billCardRv);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mBillCardsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mBillCardRVAdapter = new BillCardAdapter(
                mBillViewModel.getBills().getValue() // Passem-li referencia llista usuaris
        );

        mBillCardRVAdapter.setOnClickListener(new BillCardAdapter.OnClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClick(int position, View view) {
                LinearLayout ll = view.findViewById(R.id.extraBillDataLayout);
                int v = (ll.getVisibility() ==View.GONE)? View.VISIBLE: View.GONE;

                TransitionManager.beginDelayedTransition(view.findViewById(R.id.cardBill), new AutoTransition());
                ll.setVisibility(v);

            }
        });

        mBillCardRVAdapter.setOnClickDetailsListener(new BillCardAdapter.OnClickDetailsListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClickDetails(int position, String billID, Bill bill) {
                Intent intent = new Intent(activity, BillDetailsActivity.class);
                intent.putExtra("bill", bill);
                startActivity(intent);
            }
        });

        mBillCardRVAdapter.setOnLongClickListener(new BillCardAdapter.OnLongClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnLongClick(String billID, int position, Bill bill) {
                MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(getContext(), R.style.MaterialAlertDialog_rounded)
                        .setTitle("Eliminar gasto " + bill.getName()).setMessage("Estas seguro de que quieres eliminar el gasto " + bill.getName())
                        .setPositiveButton("Aceptar", (dialog, which) -> {mBillViewModel.deleteBill(billID);}).setNegativeButton("Cancelar", (dialog, which)->{})/*.create()*/;
                madb.create().show();

            }
        });


        mBillCardsRV.setAdapter(mBillCardRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Bill>> observerBills = new Observer<ArrayList<Bill>>() {
            @Override
            public void onChanged(ArrayList<Bill> bills) {
                mBillCardRVAdapter.setBill(bills);
                mBillCardRVAdapter.notifyDataSetChanged();
            }
        };
        mBillViewModel.getBills().observe(getViewLifecycleOwner(), observerBills);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mBillViewModel.loadBillsFromRepository(groupID);  // Internament pobla els usuaris de la BBDD


        mBillViewModel.addListenerFirebaseChanges(groupID, mGroup.getMembers().getArray());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

    }
}
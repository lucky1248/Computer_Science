package ub.edu.moneysplitter.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.BillViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BalanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BalanceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private BillViewModel mBillViewModel;


    private Group mGroup;
    private RecyclerView mBalanceCardsRV; // RecyclerView

    /** Adapter de la RecyclerView */
    private BalanceCardAdapter mBalanceCardRVAdapter;

    public BalanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BalanceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BalanceFragment newInstance(String param1, String param2) {
        BalanceFragment fragment = new BalanceFragment();
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
        View view = inflater.inflate(R.layout.fragment_balance, container, false);




        GroupActivity activity = (GroupActivity) getActivity();
        String groupID = activity.getgroupID();
        this.mGroup = activity.getgroup();


        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        mBillViewModel = activity.getViewModel();//new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mBalanceCardsRV = view.findViewById(R.id.balanceCardRv);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mBalanceCardsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mBalanceCardRVAdapter = new BalanceCardAdapter(
                mBillViewModel.getBalance().getValue() // Passem-li referencia llista usuaris
        );




        mBalanceCardsRV.setAdapter(mBalanceCardRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Pair<String, String>>> observerBalance = new Observer<ArrayList<Pair<String, String>>>() {
            @Override
            public void onChanged(ArrayList<Pair<String, String>> balance) {
                mBalanceCardRVAdapter.setBalance(balance);
                mBalanceCardRVAdapter.notifyDataSetChanged();
            }
        };
        mBillViewModel.getBalance().observe(getViewLifecycleOwner(), observerBalance);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mBillViewModel.loadBalanceFromRepository(groupID, mGroup.getMembers().getArray());  // Internament pobla els usuaris de la BBDD




        return view;
    }
}
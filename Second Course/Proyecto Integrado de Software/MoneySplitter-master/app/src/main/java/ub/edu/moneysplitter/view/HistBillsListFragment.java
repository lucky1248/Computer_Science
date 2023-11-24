package ub.edu.moneysplitter.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.viewmodel.HistorialsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistBillsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistBillsListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private HistorialsViewModel mHistViewModel;
    private BillHistCardAdapter mBillHistCardRVAdapter;
    private RecyclerView mBillHistCardRV;

    public HistBillsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistBillsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistBillsListFragment newInstance(String param1, String param2) {
        HistBillsListFragment fragment = new HistBillsListFragment();
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
        View view = inflater.inflate(R.layout.fragment_hist_bills_list, container, false);


        ExpensesActivity activity = (ExpensesActivity) getActivity();
        String userID = activity.getuserID();


        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        mHistViewModel = activity.getViewModel();//new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mBillHistCardRV = view.findViewById(R.id.HistBillsCardRv);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mBillHistCardRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mBillHistCardRVAdapter = new BillHistCardAdapter(
                mHistViewModel.getHistBills().getValue() // Passem-li referencia llista usuaris
        );




        mBillHistCardRV.setAdapter(mBillHistCardRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Bill>> observerBalance = new Observer<ArrayList<Bill>>() {
            @Override
            public void onChanged(ArrayList<Bill> balance) {
                mBillHistCardRVAdapter.setBill(balance);
                mBillHistCardRVAdapter.notifyDataSetChanged();
            }
        };
        mHistViewModel.getHistBills().observe(getViewLifecycleOwner(), observerBalance);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mHistViewModel.loadBillsHistFromRepository(userID);  // Internament pobla els usuaris de la BBDD




        return view;


    }
}
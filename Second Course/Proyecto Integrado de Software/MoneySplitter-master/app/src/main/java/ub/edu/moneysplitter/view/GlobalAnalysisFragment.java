package ub.edu.moneysplitter.view;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.viewmodel.HistorialsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GlobalAnalysisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GlobalAnalysisFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final ArrayList<String> al = new ArrayList<>(); //array del spinner
    private String filtre = "T";
    private Spinner mSpinnerFilter;
    private HistorialsViewModel mHistViewModel;
    private RecyclerView mAnalysisGlobalCardsRV;
    private GlobalAnalysisCardAdapter mAnalysisGlobalCardsRVAdapter;
    private TextView analisisGlobalTotal;
    public GlobalAnalysisFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GlobalAnalysisFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GlobalAnalysisFragment newInstance(String param1, String param2) {
        GlobalAnalysisFragment fragment = new GlobalAnalysisFragment();
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

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_global_analysis, container, false);
        //OPCION DE FILTRAT
        al.add("Total");
        al.add("Semana");
        al.add("Mes");
        al.add("Año");//hacer un array global asi mes adaptable a nouevos cambios
        ArrayAdapter<String> adapterPayM = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, al);
        adapterPayM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerFilter = (Spinner) view.findViewById(R.id.spinnerAnalysis);
        mSpinnerFilter.setAdapter(adapterPayM);


        ExpensesActivity activity = (ExpensesActivity) getActivity();
        String userID = activity.getuserID();
        mHistViewModel = activity.getViewModel();


        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        //new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mAnalysisGlobalCardsRV = view.findViewById(R.id.GlobalAnCardRv);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        );
        mAnalysisGlobalCardsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mAnalysisGlobalCardsRVAdapter = new GlobalAnalysisCardAdapter(
                mHistViewModel.getGroupsByDate().getValue() // Passem-li referencia llista usuaris
        );




        mAnalysisGlobalCardsRV.setAdapter(mAnalysisGlobalCardsRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Map<String, String>>> observerBalance = new Observer<ArrayList<Map<String, String>>>() {
            @Override
            public void onChanged(ArrayList<Map<String, String>> balance) {
                mAnalysisGlobalCardsRVAdapter.setGroupsFiltered(balance);
                mAnalysisGlobalCardsRVAdapter.notifyDataSetChanged();
            }
        };
        mHistViewModel.getGroupsByDate().observe(getViewLifecycleOwner(), observerBalance);

        analisisGlobalTotal = view.findViewById(R.id.analisisGlobalTotal);
        mHistViewModel.setOnGetExpensesTotalListener(new HistorialsViewModel.OnGetExpensesTotalListener() {
            @Override
            public void OnGetExpensesTotal(String totalExpenses) {
                analisisGlobalTotal.setText(totalExpenses + " €");
            }
        });

        mSpinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                filtre = String.valueOf(al.get(position).charAt(0));
                mHistViewModel.loadGroupsByDateFromRepository(userID, filtre);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }


        });

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        //mHistViewModel.loadGroupsByDateFromRepository(userID, filtre);  // Internament pobla els usuaris de la BBDD









        return view;
    }
}
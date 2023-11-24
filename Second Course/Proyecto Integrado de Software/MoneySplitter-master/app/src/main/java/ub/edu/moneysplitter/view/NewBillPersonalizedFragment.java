package ub.edu.moneysplitter.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewBillPersonalizedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewBillPersonalizedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LinearLayout mNewMembersLayout;
    private Button mNewBillButton;

    public NewBillPersonalizedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewBillPersonalizedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewBillPersonalizedFragment newInstance(String param1, String param2) {
        NewBillPersonalizedFragment fragment = new NewBillPersonalizedFragment();
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
        View view = inflater.inflate(R.layout.fragment_new_bill_personalized, container, false);
        NewBillActivity nba = (NewBillActivity) getActivity();


        mNewMembersLayout = view.findViewById(R.id.usersBillPaysLayout);

        Map<String, String> billUsers = new HashMap<>();
        for(String g: nba.getgroup().getMembers().getArray()){
            addMemeber(g, billUsers);
        }

        mNewBillButton = view.findViewById(R.id.buttonNewBillFinal);


        mNewBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //updatebillUsers(nba, billUsers);
                nba.createBill(billUsers);

            }
        });

        return view;
    }


    //see this way man
    /*private void updatebillUsers(NewBillActivity nba, Map<String, String> billUsers, View view) {
        for(String g: nba.getgroup().getMembers().getArray()){
            billUsers.put(g, view.findViewById(R.id.))
        }
    }*/


    private void addMemeber(String tlf, Map<String, String> billUsers){

        //newGroupMembers.add(tlf);

        LayoutInflater inflater = LayoutInflater.from(mNewMembersLayout.getContext());

        //Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View v = inflater.inflate(R.layout.insert_user_bill_view, mNewMembersLayout, false); //le pasociamos al padre



        //arreglar este desastre
        ((EditText)v.findViewById(R.id.editTextUserBill)).addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                billUsers.put(UserViewModel.getUserID(/*tlf.substring(0, tlf.length()-1)*/tlf), s.toString()/*checkout*/);


            }
        });
        TextView number = v.findViewById(R.id.userBillText);
        //number.setId(Integer.parseInt(tlf));

        number.setText(UserViewModel.getContact(tlf) + ":");
        //((EditText)v.findViewById(R.id.editTextUserBill)).setText(tlf);

        mNewMembersLayout.addView(v);
    }
}
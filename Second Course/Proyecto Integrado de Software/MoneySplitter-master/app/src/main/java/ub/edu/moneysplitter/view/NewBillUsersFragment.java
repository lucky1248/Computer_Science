package ub.edu.moneysplitter.view;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewBillUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewBillUsersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private LinearLayout mNewMembersLayout;
    private Button mNewBillButton;

    private EditText mTotalPrice;

    public NewBillUsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewBillUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewBillUsersFragment newInstance(String param1, String param2) {
        NewBillUsersFragment fragment = new NewBillUsersFragment();
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
        View view = inflater.inflate(R.layout.fragment_new_bill_users, container, false);
        NewBillActivity nba = (NewBillActivity) getActivity();


        mNewMembersLayout = view.findViewById(R.id.linearLayoutMembresIncl);

        Map<String, Boolean> billUsers = new HashMap<>();

        for(String g: nba.getgroup().getMembers().getArray()){
            billUsers.put(g, false);
            addMemeber(g, billUsers);
        }

        mNewBillButton = view.findViewById(R.id.buttonNewBillFinal);
        mTotalPrice = view.findViewById(R.id.editTextTotalBill);

        mNewBillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //updatebillUsers(nba, billUsers);
                nba.createBillUsers(billUsers, mTotalPrice.getText().toString());
            }
        });



        return view;
    }



    private void addMemeber(String tlf, Map<String, Boolean> billUsers){

        //newGroupMembers.add(tlf);

        LayoutInflater inflater = LayoutInflater.from(mNewMembersLayout.getContext());

        //Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View v = inflater.inflate(R.layout.checkbox_custom, mNewMembersLayout, false); //le pasociamos al padre





        CheckBox cb =(CheckBox)v.findViewById(R.id.checkBoxIncludedMembers);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                billUsers.put(UserViewModel.getUserID(cb.getText().toString()), isChecked);
            }
        });
        //TextView number = v.findViewById(R.id.userBillText);
        cb.setText(UserViewModel.getContact(tlf));
        //number.setId(Integer.parseInt(tlf));
        //number.setText(tlf + ":");
        //((EditText)v.findViewById(R.id.editTextUserBill)).setText(tlf);

        mNewMembersLayout.addView(v);
    }
}
package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.model.GroupBillsRepository;
import ub.edu.moneysplitter.model.MemberDebt;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.viewmodel.BillViewModel;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;

public class GroupActivity extends AppCompatActivity {

    private Button mBalanceButton;
    private Button mBillsButton;
    private Button mSettledDebtsButton;
    private String groupID;
    private String userID;
    private BillViewModel mBillViewModel;
    private Group mGroup;

    private ImageView mGroupImage;

    private TextView mGroupName;
    private TextView mGroupDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Grupo");

        Intent i = getIntent();
        groupID = i.getStringExtra("groupID");
        userID = i.getStringExtra("userID");

        mGroup = (Group) i.getSerializableExtra("group");

        mBillViewModel = new ViewModelProvider(this)
                .get(BillViewModel.class);

        GroupViewModel mGroupViewModel = new ViewModelProvider(this)
                .get(GroupViewModel.class);
        /*mGroupViewModel.addOnGetGroupListener(new GroupViewModel.OnGetGroupListener() {
            @Override
            public void OnGetGroup(Group group) {
                mGroup = group;
                loadViewGroupData();
            }
        });*/



        //mGroup = mGroupViewModel.getGroup(groupID);



        mGroupImage = findViewById(R.id.groupImageView);
        mGroupName = findViewById(R.id.groupNameTextView);
        mGroupDesc = findViewById(R.id.groupDescTextView);


        loadViewGroupData();

        mBalanceButton = findViewById(R.id.showBalanceButton);
        mBillsButton = findViewById(R.id.showBillsButton);
        mSettledDebtsButton = findViewById(R.id.saldarDeudasButton);
        mSettledDebtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBillViewModel.settleBills(userID, groupID, new BillViewModel.OnSettldDebtsListener() {
                    @Override
                    public void OnSettldDebts(MemberDebt pending, boolean settled) {
                        if(pending!=null){
                            LayoutInflater inflater = getLayoutInflater();

                            View pend = inflater.inflate(R.layout.settle_debt_dialog, null);
                            TextView price = pend.findViewById(R.id.textViewDebtAmount);
                            TextView destUser = pend.findViewById(R.id.textViewdestUser);
                            price.setText(pending.getUserDebt() + " €");
                            destUser.setText(UserContactsLocal.getInstance().getContact(pending.getDestUserID()));




                            MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(GroupActivity.this, R.style.MaterialAlertDialog_rounded).setView(pend)
                                    .setTitle("Saldar Deuda")
                                    .setPositiveButton("Aceptar", (dialog, which) -> {})/*.create()*/;
                            madb.create().show();
                        } else if(settled){

                            MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(GroupActivity.this, R.style.MaterialAlertDialog_rounded)
                                    .setTitle("Saldar Deuda").setMessage("No tienes que pagar a ningún miembro, consulta si tienes que recibir alguna cuantía en notificaciones")
                                    .setPositiveButton("Aceptar", (dialog, which) -> {})/*.create()*/;
                            madb.create().show();
                        }  else {
                            MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(GroupActivity.this, R.style.MaterialAlertDialog_rounded)
                                    .setTitle("Saldar Deuda").setMessage("No hay deudas a saldar")
                                    .setPositiveButton("Aceptar", (dialog, which) -> {})/*.create()*/;
                            madb.create().show();
                        }

                    }
                });
            }
        });

        mBalanceButton.setOnClickListener(view -> {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView,BalanceFragment.class,null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();


            mBalanceButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
            mBillsButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
        });

        mBillsButton.setOnClickListener(view -> {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView,BillsFragment.class,null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();

            mBalanceButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
            mBillsButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
        });

        //Colors definition
        mBalanceButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
        mBillsButton.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));

        mBillViewModel.listenToCurrentGroup(groupID, new BillViewModel.OnFirebaseChangesListener() {
            @Override
            public void OnFirebaseChanges(Group group)
            {
                mGroup = group;
            }
        });
    }

    private void loadViewGroupData() {
        if(mGroup!=null){
            mGroupName.setText(mGroup.getName());
            mGroupDesc.setText(mGroup.getDescription());
            if (mGroup.getURL()!=null && !mGroup.getURL().equals("")) {
                Picasso.get().load(mGroup.getURL()).into(mGroupImage);
            }
        }

    }

    public String getgroupID() {

        return groupID;
    }

    public Group getgroup() {

        return mGroup;
    }

    public BillViewModel getViewModel() {
        return mBillViewModel;
    }

    public String getuserID() {
        return userID;
    }
}
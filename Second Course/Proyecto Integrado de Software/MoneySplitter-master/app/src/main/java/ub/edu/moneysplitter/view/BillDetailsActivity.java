package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.UserBill;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.viewmodel.BillMembersViewModel;

public class BillDetailsActivity extends AppCompatActivity {



    private TextView mCardName;
    private TextView mCardPrice;
    private TextView mCardDate;
    private TextView mCardHour;
    private TextView mCardPayer;
    private Bill mBill;
    private ImageView mCardImage;

    private RecyclerView mBillMembersCardsRV; // RecyclerView

    /** Adapter de la RecyclerView */
    private BillMemberCardAdapter mBillMemberCardRVAdapter;
    private BillMembersViewModel mBillMembersViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_details);



        Intent intent = getIntent();
        mBill = (Bill) intent.getSerializableExtra("bill");

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Detalls de Despesa");


        getViewItems();
        setViewData();

        mBillMembersViewModel = new ViewModelProvider(this)
                .get(BillMembersViewModel.class);

        mBillMembersCardsRV = findViewById(R.id.userBillRv);
        UserBillRecyclerInit();
    }

    private void UserBillRecyclerInit() {


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                BillDetailsActivity.this, LinearLayoutManager.VERTICAL, false
        );
        mBillMembersCardsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mBillMemberCardRVAdapter = new BillMemberCardAdapter(
                mBillMembersViewModel.getBillMembers().getValue() // Passem-li referencia llista usuaris
        );


        mBillMembersCardsRV.setAdapter(mBillMemberCardRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<UserBill>> observerGroups = new Observer<ArrayList<UserBill>>() {
            @Override
            public void onChanged(ArrayList<UserBill> users) {

                mBillMemberCardRVAdapter.notifyDataSetChanged();
            }
        };
        mBillMembersViewModel.getBillMembers().observe(this, observerGroups);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mBillMembersViewModel.loadBillMemebersFromRepository(mBill.getID());  // Internament pobla els usuaris de la BBDD


    }

    private void setViewData() {

        mCardName.setText(mBill.getName());
        mCardPrice.setText(String.valueOf(mBill.getPrice()) + " €");
        mCardDate.setText(mBill.getDate());
        mCardHour.setText(mBill.getHour());
        mCardPayer.setText("Pagó: " + UserContactsLocal.getInstance().getContact(mBill.getPayerName()));
        if (mBill.getPictureUrl()!=null && !mBill.getPictureUrl().equals("")) {
            Picasso.get().load(mBill.getPictureUrl()).into(mCardImage);
        }
    }

    private void getViewItems() {
        mCardName = findViewById(R.id.billNameTextDet);
        mCardPrice = findViewById(R.id.billPriceTextDet);
        mCardDate = findViewById(R.id.billDateTextDet);
        mCardHour = findViewById(R.id.billTimeTextDet);
        mCardPayer = findViewById(R.id.billPayerTextDet);
        mCardImage = findViewById(R.id.billImageDet);
    }
}
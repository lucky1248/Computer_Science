package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Map;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;
import ub.edu.moneysplitter.viewmodel.HistorialsViewModel;

public class PaymentsActivity extends AppCompatActivity {
    private ImageView mPerfil;

    private HistorialsViewModel mHistViewModel;
    private String userID;
    private RecyclerView mPaymentCardsRV; // RecyclerView
    private PaymentsCardAdapter mPaymentCardAdapter;

    /** Adapter de la RecyclerView */
    private GroupCardAdapter mPaymentCardRVAdapter;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Histórico de pagos");

        Intent i = getIntent();
        userID = i.getStringExtra("userID");

        mPerfil = findViewById(R.id.imageProfile);
        mPerfil.setOnClickListener(view -> {
            finish();
            /*Intent intent = new Intent(PaymentsActivity.this, ProfileActivity.class);
            startActivity(intent);*/
        });


        mHistViewModel = new ViewModelProvider(this)
                .get(HistorialsViewModel.class);


        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)
        //new ViewModelProvider(this)





        // Anem a buscar la RecyclerView i fem dues coses:
        mPaymentCardsRV = findViewById(R.id.paymentsCardRv);


        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                PaymentsActivity.this, LinearLayoutManager.VERTICAL, false
        );
        mPaymentCardsRV.setLayoutManager(manager);

        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mPaymentCardAdapter = new PaymentsCardAdapter(
                mHistViewModel.getPayments().getValue() // Passem-li referencia llista usuaris
        );




        mPaymentCardsRV.setAdapter(mPaymentCardAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Map<String, String>>> observerBalance = new Observer<ArrayList<Map<String, String>>>() {
            @Override
            public void onChanged(ArrayList<Map<String, String>> balance) {
                mPaymentCardAdapter.setPayments(balance);
                mPaymentCardAdapter.notifyDataSetChanged();
            }
        };
        mHistViewModel.getPayments().observe(this, observerBalance);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mHistViewModel.loadPaymentsFromRepository(userID);  // Internament pobla els usuaris de la BBDD






    }
}
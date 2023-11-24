package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.view.GroupCardAdapter;
import ub.edu.moneysplitter.view.ProfileActivity;
import ub.edu.moneysplitter.viewmodel.HistorialsViewModel;

public class ExpensesActivity extends AppCompatActivity {

    private ImageView mPerfil;
    private String userID;

    private HistorialsViewModel mHistViewModel;

    /** Adapter de la RecyclerView */
    private GroupCardAdapter mExpenseCardRVAdapter;
    private Button mListado;
    private Button mAnalisis;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Histórico de gastos");

        Intent i = getIntent();
        userID = i.getStringExtra("userID");

        mHistViewModel = new ViewModelProvider(this)
                .get(HistorialsViewModel.class);

        mPerfil = findViewById(R.id.imageProfile);
        mListado = findViewById(R.id.listadoGastosButton);
        mAnalisis = findViewById(R.id.analisisGlobalButton);

        mListado.setOnClickListener(view -> {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentExpensesContianer,HistBillsListFragment.class,null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            mListado.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
            mAnalisis.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
        });

        mAnalisis.setOnClickListener(view -> {

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentExpensesContianer,GlobalAnalysisFragment.class,null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            mAnalisis.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));
            mListado.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
        });

        mAnalisis.setBackgroundTintList(this.getResources().getColorStateList(R.color.white));
        mListado.setBackgroundTintList(this.getResources().getColorStateList(R.color.soft_blue));


        mPerfil = findViewById(R.id.imageProfile);
        mPerfil.setOnClickListener(view -> {
            finish();/*Intent intent = new Intent(ExpensesActivity.this, ProfileActivity.class);
            startActivity(intent);*/
        });





    }

    public String getuserID() {
        return userID;
    }

    public HistorialsViewModel getViewModel() {
        return mHistViewModel;
    }
}

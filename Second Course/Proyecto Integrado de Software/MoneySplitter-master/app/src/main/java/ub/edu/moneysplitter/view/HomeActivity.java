package ub.edu.moneysplitter.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;


import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.model.SaveSharedPreference;
import ub.edu.moneysplitter.model.User;
import ub.edu.moneysplitter.model.UserContactsLocal;
import ub.edu.moneysplitter.viewmodel.GroupViewModel;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class HomeActivity extends AppCompatActivity {

    private User mUser;
    private String userID;
    private ImageView mImage;
    private GroupViewModel mGroupViewModel;
    private UserViewModel mUserViewModel;


    private TextView mTextBenvinguda;
    private RecyclerView mGroupCardsRV; // RecyclerView

    /** Adapter de la RecyclerView */
    private GroupCardAdapter mGroupCardRVAdapter;

    /** Autenticació de Firebase */
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    View home_groups;
    private FloatingActionButton mNewGroup;
    private ImageButton mSearchBtn;

    private ImageView mPerfil;
    private EditText mSearchFilter;
    private ConstraintLayout mNotifiacions;
    private ConstraintLayout mNotifiacionsAction;
    private LinearLayout currentLayout;

    //NOTIFICATIONS BUTTON
    private Button newGroupsNoti;
    private Button newBillsNoti;
    private Button newPendingPaysNoti;
    private Button newRecievedPaysNoti;
    private String isOldLogged = "false";
    private String isOldLoggedBills = "false";
    private String isOldLoggedPending = "false";
    private String isOldLoggedRecieved = "false";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        home_groups = getLayoutInflater().inflate(R.layout.activity_home_groups, null);
        setContentView(home_groups);








        currentLayout = findViewById(R.id.HomeGroups);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_blue)));
        getSupportActionBar().setTitle("Home");

        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
        if(SaveSharedPreference.getNotificationsStatus(HomeActivity.this)){
            isOldLogged = intent.getStringExtra("isOldLogged");
            isOldLoggedBills = intent.getStringExtra("isOldLogged");
            isOldLoggedPending = intent.getStringExtra("isOldLogged");
            isOldLoggedRecieved = intent.getStringExtra("isOldLogged");
        } else {
            isOldLogged = "false";
            isOldLoggedBills = "false";
            isOldLoggedPending = "false";
            isOldLoggedRecieved = "false";
        }



        UserContactsLocal.getInstance().setUserID(userID);



        mGroupViewModel = new ViewModelProvider(this)
                .get(GroupViewModel.class);
        mUserViewModel = new ViewModelProvider(this)
                .get(UserViewModel.class);
        setNotificationButtons();


        mSearchFilter = findViewById(R.id.editTextsearchbar);
        mSearchBtn = findViewById(R.id.searchButton);
        mSearchBtn.setOnClickListener(view -> {

            mGroupViewModel.filterGroupsBy(mSearchFilter.getText().toString().toLowerCase());
        });

        mSearchFilter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                mGroupViewModel.filterGroupsBy(mSearchFilter.getText().toString().toLowerCase());

            }
        });

        mNotifiacionsAction = findViewById(R.id.notificationsAction);
        mNotifiacions = findViewById(R.id.notifications);
        mNotifiacionsAction.setOnClickListener(view -> {

            showUnshowNotifications();
        });





        //NO TENEMOS MODO NOCHE A CHUPARLA
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mNewGroup = findViewById(R.id.addNewGroupButton);
        mNewGroup.setOnClickListener(view -> {
            Intent i = new Intent(HomeActivity.this, NewGroupActivity.class);
            i.putExtra("userID", userID);
            startActivity(i);
        });

        mImage = findViewById(R.id.imageProfile);

        //AGAFEM LES DADES DEL USUARI ACTUAL
        mTextBenvinguda = findViewById(R.id.textBenvinguda);
        mUserViewModel.addOnGetUserListener(new UserViewModel.OnGetUserListener() {
            @Override
            public void OnGetUser(User users) {
                mUser = users;
                String wTxt = "Hola, " + users.getFirstName();
                CharSequence txt = wTxt;
                mTextBenvinguda.setText(txt);
                paintImageView(users.getURL());

            }
        });

        mUserViewModel.getUser(userID);


        // Inicialitza el ViewModel d'aquesta activity (HomeActivity)


        // Anem a buscar la RecyclerView i fem dues coses:
        mGroupCardsRV = findViewById(R.id.groupCardRv);

        // (1) Li assignem un layout manager.
        LinearLayoutManager manager = new LinearLayoutManager(
                HomeActivity.this, LinearLayoutManager.VERTICAL, false
        );
        mGroupCardsRV.setLayoutManager(manager);






        // (2) Inicialitza el RecyclerViewAdapter i li assignem a la RecyclerView.
        mGroupCardRVAdapter = new GroupCardAdapter(
                mGroupViewModel.getGroups().getValue() // Passem-li referencia llista usuaris
        );

        mGroupCardRVAdapter.setOnClickListener(new GroupCardAdapter.OnClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnClick(String groupID, int position, Group group) {
                Intent intent = new Intent(HomeActivity.this, GroupActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("group", group);
                intent.putExtra("userID", userID);
                startActivity(intent);

            }
        });

        mGroupCardRVAdapter.setOnLongClickListener(new GroupCardAdapter.OnLongClickListener() {
            // Listener que escoltarà quan interactuem amb un item en una posició donada
            // dins de la recicler view. En aquest cas, quan es faci clic al botó d'amagar
            // l'usuari.
            @Override
            public void OnLongClick(String groupID, int position, Group group) {
                MaterialAlertDialogBuilder madb  = new MaterialAlertDialogBuilder(HomeActivity.this, R.style.MaterialAlertDialog_rounded)
                        .setTitle("Eliminar grupo " + group.getName()).setMessage("Estas seguro de que quieres eliminar el grupo " + group.getName())
                        .setPositiveButton("Aceptar", (dialog, which) -> {mGroupViewModel.deleteGroup(groupID);}).setNegativeButton("Cancelar", (dialog, which)->{})/*.create()*/;
                madb.create().show();

            }
        });

        mGroupCardsRV.setAdapter(mGroupCardRVAdapter); // Associa l'adapter amb la ReciclerView

        // Observer a HomeActivity per veure si la llista de User (observable MutableLiveData)
        // a GroupViewModel ha canviat.
        final Observer<ArrayList<Group>> observerGroups = new Observer<ArrayList<Group>>() {
            @Override
            public void onChanged(ArrayList<Group> groups) {
                mGroupCardRVAdapter.setGroups(groups);
                mGroupCardRVAdapter.notifyDataSetChanged();
            }
        };
        mGroupViewModel.getGroups().observe(this, observerGroups);

        // A partir d'aquí, en cas que es faci cap canvi a la llista d'usuaris, HomeActivity ho sabrá
        mGroupViewModel.loadGroupsFromRepositoryFiltered(userID);  // Internament pobla els usuaris de la BBDD


        mGroupViewModel.addListenerFirebaseChanges(userID);

        mPerfil = findViewById(R.id.imageProfile);
        mPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                intent.putExtra("mUser", mUser);
                startActivity(intent);
            }
        });

    }

    private void paintImageView(String userUrl){
        if (userUrl!=null && !userUrl.equals("")) {
            Picasso.get().load(userUrl).into(mImage);
        }
    }

    private void setNotificationButtons() {




        newGroupsNoti = findViewById(R.id.btnNewGroups);
        newGroupsNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newGroupsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text));
                new NewGroupsDialog().show(getSupportFragmentManager(), "PurchaseConfirmationDialog.TAG");
            }
        });

        newBillsNoti = findViewById(R.id.btnNuevosGastos);
        newBillsNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newBillsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text));
                new NewBillsDialog().show(getSupportFragmentManager(), "PurchaseConfirmationDialog.TAG");
            }
        });

        newPendingPaysNoti = findViewById(R.id.btnPagosPendientes);
        newPendingPaysNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newPendingPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text));
                new NewPendingPaysDialog().show(getSupportFragmentManager(), "PurchaseConfirmationDialog.TAG");
            }
        });

        newRecievedPaysNoti = findViewById(R.id.btnPagosRecibir);
        newRecievedPaysNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                newRecievedPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text));
                new NewRecievedPaysDialog().show(getSupportFragmentManager(), "PurchaseConfirmationDialog.TAG");
            }
        });


        //THE NUMBER OF NOTIFICATIONS
        mGroupViewModel.addListenerFirebaseNotificationsChanges(userID, new GroupViewModel.OnFirebaseChangesNewGroupsListener() {
            @Override
            public void OnFirebaseChangesNewGroups(int numGroups) {
                /*if(isOldLogged.equals("true")){
                    isOldLogged="false";
                    if(SaveSharedPreference.getNotificationsStatus(HomeActivity.this) && Integer.parseInt(SaveSharedPreference.getnewGroups(HomeActivity.this))!=numGroups){
                        newGroupsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));
                    }
                } else {
                    newGroupsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));

                }
                SaveSharedPreference.setGroupsNotifications(HomeActivity.this, true, String.valueOf(numGroups));*/
                newGroupsNoti.setText(numGroups + " Grupos nuevos");
            }
        }, new GroupViewModel.OnFirebaseChangesNewBillsListener() {
            @Override
            public void OnFirebaseChangesNewBills(int numBills) {
                /*if(isOldLoggedBills.equals("true")){
                    isOldLoggedBills="false";
                    if(SaveSharedPreference.getNotificationsStatus(HomeActivity.this) && Integer.parseInt(SaveSharedPreference.getnewGroups(HomeActivity.this))!=numBills){
                        newGroupsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));
                    }
                } else {
                    newGroupsNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));

                }
                SaveSharedPreference.setBillsNotifications(HomeActivity.this, true, String.valueOf(numBills));*/
                newBillsNoti.setText(numBills + " Gastos nuevos");
            }
        }, new GroupViewModel.OnFirebaseChangesNewSettleDebtsListener() {
            @Override
            public void OnFirebaseChangesNewSettleDebts(int numPendingPays, int numRecievedPays) {
                /*if(isOldLoggedPending.equals("true")){
                    isOldLoggedPending="false";
                    if(SaveSharedPreference.getNotificationsStatus(HomeActivity.this) && Integer.parseInt(SaveSharedPreference.getnewGroups(HomeActivity.this))!=numPendingPays){
                        newPendingPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));
                    }
                } else {
                    newPendingPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));

                }
                SaveSharedPreference.setPendingNotifications(HomeActivity.this, true, String.valueOf(numPendingPays));
                if(isOldLoggedRecieved.equals("true")){
                    isOldLoggedRecieved="false";
                    if(SaveSharedPreference.getNotificationsStatus(HomeActivity.this) && Integer.parseInt(SaveSharedPreference.getnewGroups(HomeActivity.this))!=numRecievedPays){
                        newRecievedPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));
                    }
                } else {
                    newRecievedPaysNoti.setBackground(ContextCompat.getDrawable(HomeActivity.this, R.drawable.rounded_corners_plain_text_notifications));

                }
                SaveSharedPreference.setRecievedNotifications(HomeActivity.this, true, String.valueOf(numRecievedPays));*/
                newPendingPaysNoti.setText(numPendingPays + " Pagos pendientes");
                newRecievedPaysNoti.setText(numRecievedPays + " Pagos por recibir");
            }
        });
    }

    private void showUnshowNotifications() {
        int v = 0;

        if(mNotifiacions.getVisibility() ==View.GONE){
            v = View.VISIBLE;
            findViewById(R.id.dropArrowImage).setBackground(this.getDrawable(R.drawable.baseline_arrow_drop_up_48));
        } else {
            v = View.GONE;
            findViewById(R.id.dropArrowImage).setBackground(this.getDrawable(R.drawable.baseline_arrow_drop_down_48));
        }

        TransitionManager.beginDelayedTransition(currentLayout, new AutoTransition());
        mNotifiacions.setVisibility(v);
    }


    public String getUserID() {
        return userID;
    }

    public GroupViewModel getViewModel() {
        return mGroupViewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();

        updatePersonalData();
    }

    private void updatePersonalData() {
        mUserViewModel.addOnGetUserListener(new UserViewModel.OnGetUserListener() {
            @Override
            public void OnGetUser(User users) {
                mUser = users;
                String wTxt = "Hola, " + users.getFirstName();
                CharSequence txt = wTxt;
                mTextBenvinguda.setText(txt);
                paintImageView(users.getURL());

            }
        });
        mUserViewModel.getUser(userID);
    }
}
package ub.edu.moneysplitter.model;

import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.base.StandardSystemProperty;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class SettleGroupBills {


    private static final String TAG = "SettleGroupBills";
    private static final SettleGroupBills mInstance = new SettleGroupBills();

    /**
     * Referència a la Base de Dades
     */
    private FirebaseFirestore mDb;

    private SettleGroupBills() {
        mDb = FirebaseFirestore.getInstance();
    }

    public static SettleGroupBills getInstance() {
        return mInstance;
    }


    //LISTEN NOTIFICATIONS
    public interface OnFirebaseChangesNewSettleDebtsListener {

        public void OnFirebaseChangesNewSettleDebts(int numPendingPays, int numRecievedPays);
    }

    public void listenFirebaseChangesSettleDebts(String userID, SettleGroupBills.OnFirebaseChangesNewSettleDebtsListener onFirebaseChangesNewSettleDebtsListener) {

        mDb.collection("settledNotifications").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    //Log.w(TAG, "Listen failed.", e);
                    return;
                }

                int numPend = 0;//((ArrayList<Map<String, String>>) snapshot.getData().get("groups")).size();
                int numReci = 0;
                for (QueryDocumentSnapshot doc : value) {
                    if(doc.getId().equals(userID)) {

                        if((doc.get("settledDebts") instanceof ArrayList))
                            numPend = ((ArrayList<Map<String, String>>) doc.get("settledDebts")).size();

                    } else {
                        ArrayList<Map<String, String>> items = (ArrayList<Map<String, String>>) doc.get("settledDebts");
                        for(Map<String, String> data: items){
                            if(data.get("destUserID").equals(userID))
                                numReci++;
                            }
                        }
                    }
                onFirebaseChangesNewSettleDebtsListener.OnFirebaseChangesNewSettleDebts(numPend, numReci);
                }

                //Log.d(TAG, "Current cites in CA: " + cities);
            });
        //});

    }


    public interface OnSettldDebtsListener {
        void OnSettldDebts(ArrayList<MemberDebt> settledData); //un map amb el balanç de cada membre
    }

    public ArrayList<SettleGroupBills.OnSettldDebtsListener> mOnSettldDebtsListener = new ArrayList<>();


    public interface OnGetDebtsListener {
        void OnGetDebts(Map<String, Float> usersDebt); //un map amb el balanç de cada membre
    }


    public interface OnGetPayHistorialListener {
        void OnGetPayHistorial(ArrayList<Map<String, String>> hist); //un map amb el balanç de cada membre
    }
    //public ArrayList<SettleGroupBills.OnSettldDebtsListener> mOnSettldDebtsListener = new ArrayList<>();


    public void getGroupDebts(String groupID, OnSettldDebtsListener listener) {

        DocumentReference docRef = mDb.collection("GroupBills").document(groupID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    ArrayList<String> billIDs = (ArrayList<String>) document.get("bills");
                    SettleGroupBills.OnGetDebtsListener getListener = new SettleGroupBills.OnGetDebtsListener() {
                        @Override
                        public void OnGetDebts(Map<String, Float> usersDebt) {
                            ArrayList<MemberDebt> settledData = balanceSetter(usersDebt);
                            for (MemberDebt md : settledData) {
                                Date date = new Date();
                                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                                calendar.setTime(date);
                                md.setDebtID(groupID + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + ":" + calendar.get(Calendar.MILLISECOND));
                                sendSettledDataNoti(md);
                            }


                            listener.OnSettldDebts(settledData);
                        }
                    };

                    getGroupBalance(billIDs, getListener);
                } else {
                    //Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });


    }

    private void sendSettledDataNoti(MemberDebt md) {
        DocumentReference docRef = mDb.collection("settledNotifications").document(md.getUserID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        ArrayList<Map<String, String>> l = (ArrayList<Map<String, String>>) document.get("settledDebts");
                        //l.add(userID);
                        Map<String, String> debt = new HashMap<>();
                        debt.put("destUserID", md.getDestUserID());
                        debt.put("debt", md.getUserDebt());
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = new Date();
                        debt.put("date", formatter.format(date));
                        debt.put("debtID", md.getDebtID());

                        l.add(debt);
                        document.getReference().update("settledDebts", l);
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getPendingPaysNoti(String userID, OnSettldDebtsListener listener) {
        DocumentReference docRef = mDb.collection("settledNotifications").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        ArrayList<Map<String, String>> l = (ArrayList<Map<String, String>>) document.get("settledDebts");
                        //l.add(userID);
                        ArrayList<MemberDebt> userDebt = new ArrayList<>();
                        for (Map<String, String> data : l) {
                            MemberDebt md = new MemberDebt(userID, Float.valueOf(data.get("debt")), data.get("destUserID"));
                            md.setDebtID(data.get("debtID"));
                            md.setDate(data.get("date"));
                            userDebt.add(md);

                        }


                        listener.OnSettldDebts(userDebt);
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void getRecivedPaysNoti(String userID, OnSettldDebtsListener listener) {

        mDb.collection("settledNotifications").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<MemberDebt> userDebt = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        //si la deuda no ha estado saldada y es del grupo
                        ArrayList<Map<String, String>> l = (ArrayList<Map<String, String>>) document.get("settledDebts");
                        //l.add(userID);

                        for (Map<String, String> data : l) {
                            if (data.get("destUserID").equals(userID)) {
                                MemberDebt md = new MemberDebt(document.getId(), Float.valueOf(data.get("debt")), data.get("destUserID"));
                                md.setDebtID(data.get("debtID"));
                                md.setDate(data.get("date"));
                                userDebt.add(md);

                            }


                        }
                    }
                    /* Callback listeners */

                    listener.OnSettldDebts(userDebt);
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    //no van a haber dos deudas de la misma fecha y destinatario
    public void deleteSettledDataNoti(MemberDebt md) {
        DocumentReference docRef = mDb.collection("settledNotifications").document(md.getUserID());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        ArrayList<Map<String, String>> l = (ArrayList<Map<String, String>>) document.get("settledDebts");
                        //l.add(userID);
                        ArrayList<Map<String, String>> definiveDebts = (ArrayList<Map<String, String>>) l.clone();
                        for (Map<String, String> debt : l) {
                            if (debt.get("debtID").equals(md.getDebtID())) {
                                //see how to ensure unicity (1 debt for a group and a moment)
                                definiveDebts.remove(debt);
                            }
                        }
                        document.getReference().update("settledDebts", definiveDebts);
                        addPaymentHistoral(md);
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    //PER PODER OBTENIR UN HISTORIAL DE PAGAMENTS
    private void addPaymentHistoral(MemberDebt md) {
        Map<String, Object> signedUpUser = new HashMap<>();
        signedUpUser.put("payerID", md.getUserID());
        signedUpUser.put("amount", md.getUserDebt());
        signedUpUser.put("recieverID", md.getDestUserID());
        signedUpUser.put("date", md.getDate());

        // Afegir-la a la base de dades
        mDb.collection("payments").document().set(signedUpUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Payment load completion succeeded");
                        } else {
                            Log.d(TAG, "Payment load completion failed");
                        }
                    }
                });
    }

    public void getPaymentsHistorial(ArrayList<Map<String, String>> payHistorial, String userID, OnGetPayHistorialListener onGetPayHistorialListener){
        payHistorial.clear();
        mDb.collection("payments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if(document.get("payerID").equals(userID)){
                            Map<String, String> pay = new HashMap<>();
                            pay.put("userID", (String) document.get("recieverID"));
                            pay.put("status", "P"); //payer
                            pay.put("amount", (String) document.get("amount"));
                            pay.put("date", (String) document.get("date"));
                            payHistorial.add(pay);
                        }
                        if(document.get("recieverID").equals(userID)){
                            Map<String, String> pay = new HashMap<>();
                            pay.put("userID", (String) document.get("payerID"));
                            pay.put("status", "R"); //reciever
                            pay.put("amount", (String) document.get("amount"));
                            pay.put("date", (String) document.get("date"));
                            payHistorial.add(pay);
                        }
                        onGetPayHistorialListener.OnGetPayHistorial(payHistorial);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    public void getGroupBalance(ArrayList<String> billIDs, SettleGroupBills.OnGetDebtsListener getListener) {
        mDb.collection("Bills").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Float> usersDebt = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        //si la deuda no ha estado saldada y es del grupo
                        if (!((Boolean) document.get("settled")) && billIDs.contains(document.getId())) {
                            Map<String, String> billMembers = (Map<String, String>) document.get("billMembers");
                            for (String member : billMembers.keySet()) {
                                Float newDebt = Float.valueOf(billMembers.get(member));
                                if (usersDebt.containsKey(member)) {
                                    newDebt += usersDebt.get(member);
                                }
                                usersDebt.put(member, newDebt);
                            }

                            String member = (String) document.get("payerID");
                            Float newDebt = 0f;
                            if (usersDebt.containsKey(member)) {
                                newDebt += usersDebt.get(member);
                            }


                            Float paid = Float.valueOf(((String) document.get("price")));
                            newDebt -= paid; //no deberia ser null
                            usersDebt.put(member, newDebt);
                            document.getReference().update("settled", true);
                        }
                    }
                    /* Callback listeners */

                    getListener.OnGetDebts(usersDebt);
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    public void getGroupBalance(ArrayList<String> groupMembers, String groupID, SettleGroupBills.OnGetDebtsListener getListener) {
        DocumentReference docRef = mDb.collection("GroupBills").document(groupID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@org.checkerframework.checker.nullness.qual.NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    ArrayList<String> billIDs = (ArrayList<String>) document.get("bills");

                    getGroupBalanceWithZeros(groupMembers, billIDs, getListener);
                } else {
                    //Log.d(TAG, "Something went worong on trying to find a concrete user");
                }
            }
        });
    }

    private void getGroupBalanceWithZeros(ArrayList<String> groupMembers, ArrayList<String> billIDs, OnGetDebtsListener getListener) {
        mDb.collection("Bills").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Float> usersDebt = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        //si la deuda no ha estado saldada y es del grupo
                        if (!((Boolean) document.get("settled")) && billIDs!=null && billIDs.contains(document.getId())) {
                            Map<String, String> billMembers = (Map<String, String>) document.get("billMembers");
                            for (String member : billMembers.keySet()) {
                                Float newDebt = Float.valueOf(billMembers.get(member));
                                if (usersDebt.containsKey(member)) {
                                    newDebt += usersDebt.get(member);
                                }
                                usersDebt.put(member, newDebt);
                            }

                            String member = (String) document.get("payerID");
                            Float newDebt = 0f;
                            if (usersDebt.containsKey(member)) {
                                newDebt += usersDebt.get(member);
                            }


                            Float paid = Float.valueOf(((String) document.get("price")));
                            newDebt -= paid; //no deberia ser null
                            usersDebt.put(member, newDebt);
                        }
                    }

                    for (String member : groupMembers) {
                        if (!usersDebt.containsKey(member)) {
                            usersDebt.put(member, 0f);
                        }
                    }
                    /* Callback listeners */

                    getListener.OnGetDebts(usersDebt);
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    public void getGroupBalanceSettlingBills(String groupID, ArrayList<String> billIDs, SettleGroupBills.OnGetDebtsListener getListener) {
        mDb.collection("Bills").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Float> usersDebt = new HashMap<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d(TAG, document.getId() + " => " + document.getData());
                        //si la deuda no ha estado saldada y es del grupo
                        if (!((Boolean) document.get("settled")) && billIDs.contains(document.getId())) {
                            Map<String, String> billMembers = (Map<String, String>) document.get("billMembers");
                            for (String member : billMembers.keySet()) {
                                Float newDebt = (-1f) * Float.valueOf(billMembers.get(member));
                                if (usersDebt.containsKey(member)) {
                                    newDebt += usersDebt.get(member);
                                }
                                usersDebt.put(member, newDebt);
                            }

                            String member = (String) document.get("payerID");
                            Float newDebt = 0f;
                            if (usersDebt.containsKey(member)) {
                                newDebt += usersDebt.get(member);
                            }


                            Float paid = Float.valueOf(((String) document.get("price")));
                            newDebt += paid; //no deberia ser null
                            usersDebt.put(member, newDebt);
                            document.getReference().update("settled", true);
                        }
                    }
                    /* Callback listeners */

                    getListener.OnGetDebts(usersDebt);
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private ArrayList<MemberDebt> balanceSetter(Map<String, Float> usersDebt) {
        ArrayList<MemberDebt> settledData = new ArrayList<>();

        for (int i = 0; i < usersDebt.size(); i++) {
            Pair<String, String> p = getMinMax(usersDebt);
            MemberDebt md = new MemberDebt(p.first, usersDebt.get(p.first), p.second);

            usersDebt.put(p.second, usersDebt.get(p.first) + usersDebt.get(p.second));
            usersDebt.remove(p.first); //para evitar problemas de comparativa con el 0
            settledData.add(md);
        }
        return settledData;

    }


    private Pair<String, String> getMinMax(Map<String, Float> usersDebt) {
        Float minPos = -1f;
        String minPosMember = "";
        Float maxNeg = 1f;
        String maxNegMember = "";
        for (String member : usersDebt.keySet()) {
            if (usersDebt.get(member) >= 0) { //si es positiu el comparem amb el minim dels postitus
                if (Float.compare(minPos, -1f) == 0) {
                    minPos = usersDebt.get(member);
                    minPosMember = member;
                }
                if (minPos > usersDebt.get(member)) {
                    minPos = usersDebt.get(member);
                    minPosMember = member;
                }

            } else {
                if (Float.compare(maxNeg, 1f) == 0) {
                    maxNeg = usersDebt.get(member);
                    maxNegMember = member;
                }
                if (maxNeg < usersDebt.get(member)) {
                    maxNeg = usersDebt.get(member);
                    maxNegMember = member;
                }

            }
        }
        Pair<String, String> p = new Pair<>(minPosMember, maxNegMember);
        return p;

    }


}

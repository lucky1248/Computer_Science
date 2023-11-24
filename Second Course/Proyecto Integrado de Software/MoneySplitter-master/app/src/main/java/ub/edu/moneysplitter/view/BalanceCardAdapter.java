package ub.edu.moneysplitter.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.UserBill;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class BalanceCardAdapter extends RecyclerView.Adapter<BalanceCardAdapter.ViewHolder>{



    private ArrayList<Pair<String, String>> mBalance; // Referència a la llista de grups

    // Constructor
    public BalanceCardAdapter(ArrayList<Pair<String, String>> list) {
        this.mBalance = list; // no fa new (La llista la manté el ViewModel)

    }



    @NonNull
    @Override
    public BalanceCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.member_card_layout, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new BalanceCardAdapter.ViewHolder(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull BalanceCardAdapter.ViewHolder holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mBalance.get(position));
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {

        return mBalance.size();
    }

    /**
     * Mètode que seteja de nou la llista d'usuaris si s'hi han fet canvis de manera externa.
     * @param balance
     */
    public void setBalance(ArrayList<Pair<String, String>> balance) {
        this.mBalance = balance; // no recicla/repinta res
    }

    /**
     * Mètode que repinta la RecyclerView sencera.
     */
    public void updateBalance() {
        notifyDataSetChanged();
    }

    /**
     * Mètode que repinta només posició indicada
     * @param position
     */
    public void hideBalance(int position) {
        notifyItemRemoved(position);
    }

    /**
     * Classe ViewHolder. No és més que un placeholder de la vista (user_card_list.xml)
     * dels items de la RecyclerView. Podem implementar-ho fora de RecyclerViewAdapter,
     * però es pot fer dins.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mCardName;
        private final TextView mCardPrice;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardName = itemView.findViewById(R.id.billMemberNameText);
            mCardPrice = itemView.findViewById(R.id.billMemberBillText);

        }

        @SuppressLint("ResourceAsColor")
        public void bind(final Pair<String, String> userBalance) { //poner foto y textos

            mCardName.setText(UserViewModel.getContact(userBalance.first));
            String priceF = String.valueOf(Math.abs(Float.valueOf(userBalance.second))) + " €";

            if(Float.valueOf(userBalance.second)>0) {
                priceF = "-" + priceF;
                mCardPrice.setTextColor(Color.parseColor("#FF0000"));
            } else if(Float.valueOf(userBalance.second)<0){
                priceF = "+" + priceF;
                mCardPrice.setTextColor(Color.parseColor("#1AFF00"));
            }
            mCardPrice.setText(priceF);

            // Seteja el listener onClick del botó d'amagar (hide), que alhora
            // cridi el mètode OnClickHide que implementen els nostres propis
            // listeners de tipus OnClickHideListener.


        }
    }

}

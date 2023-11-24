package ub.edu.moneysplitter.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Map;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class PaymentsCardAdapter extends RecyclerView.Adapter<PaymentsCardAdapter.ViewHolder>{



    private ArrayList<Map<String, String>> mPayments; // Referència a la llista de grups

    // Constructor
    public PaymentsCardAdapter(ArrayList<Map<String, String>> list) {
        this.mPayments = list; // no fa new (La llista la manté el ViewModel)

    }



    @NonNull
    @Override
    public PaymentsCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.payments_card_layout, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new PaymentsCardAdapter.ViewHolder(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull PaymentsCardAdapter.ViewHolder holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mPayments.get(position));
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {

        return mPayments.size();
    }

    /**
     * Mètode que seteja de nou la llista d'usuaris si s'hi han fet canvis de manera externa.
     * @param balance
     */
    public void setPayments(ArrayList<Map<String, String>> balance) {
        this.mPayments = balance; // no recicla/repinta res
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
        private final TextView mCardDate;
        private final ImageView mSentido;

        private final TextView mCardAmount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardName = itemView.findViewById(R.id.PersonaNameText);
            mCardDate = itemView.findViewById(R.id.DateText);
            mSentido = itemView.findViewById(R.id.pagoView);
            mCardAmount = itemView.findViewById(R.id.QuantityText);
        }

        @SuppressLint("ResourceAsColor")
        public void bind(final Map<String, String> userBalance) { //poner foto y textos

            mCardName.setText(UserViewModel.getContact(userBalance.get("userID")));
            mCardDate.setText(userBalance.get("date"));
            String priceF = userBalance.get("amount") + " €";

            if(userBalance.get("status").equals("P")) {
                priceF = "-" + priceF;
                mCardAmount.setTextColor(Color.parseColor("#FF0000"));
                mSentido.setBackgroundResource(R.drawable.pago_enviado);
            } else if(userBalance.get("status").equals("R")){
                priceF = "+" + priceF;
                mCardAmount.setTextColor(Color.parseColor("#1AFF00"));
                mSentido.setBackgroundResource(R.drawable.pago_recibido);
            }
            mCardAmount.setText(priceF);

            // Seteja el listener onClick del botó d'amagar (hide), que alhora
            // cridi el mètode OnClickHide que implementen els nostres propis
            // listeners de tipus OnClickHideListener.


        }
    }

}

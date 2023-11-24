package ub.edu.moneysplitter.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class BillHistCardAdapter extends RecyclerView.Adapter<BillHistCardAdapter.ViewHolder>{




    private ArrayList<Bill> mBills; // Referència a la llista de grups


    // Constructor
    public BillHistCardAdapter(ArrayList<Bill> userList) {
        this.mBills = userList; // no fa new (La llista la manté el ViewModel)

    }


    @NonNull
    @Override
    public BillHistCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.member_card_layout_bills_hist, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new BillHistCardAdapter.ViewHolder(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull BillHistCardAdapter.ViewHolder holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mBills.get(position));
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {
        return mBills.size();
    }

    /**
     * Mètode que seteja de nou la llista d'usuaris si s'hi han fet canvis de manera externa.
     * @param bills
     */
    public void setBill(ArrayList<Bill> bills) {
        this.mBills = bills; // no recicla/repinta res
    }

    /**
     * Mètode que repinta la RecyclerView sencera.
     */
    public void updateBills() {
        notifyDataSetChanged();
    }

    /**
     * Mètode que repinta només posició indicada
     * @param position
     */
    public void hideBill(int position) {
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

        public void bind(final Bill bill) { //poner foto y textos
            mCardName.setText(bill.getGroupName() + "-" + bill.getName() + "-" + bill.getDate());
            mCardPrice.setText(bill.getPrice() + " €");


        }
    }

}

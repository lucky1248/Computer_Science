package ub.edu.moneysplitter.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;
import ub.edu.moneysplitter.viewmodel.UserViewModel;

public class BillCardAdapter extends RecyclerView.Adapter<BillCardAdapter.ViewHolder>{

    public interface OnClickListener {
        void OnClick(int position, View view);
    }

    public interface OnClickDetailsListener {
        void OnClickDetails(int position, String billID, Bill bill);
    }

    public interface OnLongClickListener {
        void OnLongClick(String billID, int position, Bill bill);
    }


    private ArrayList<Bill> mBills; // Referència a la llista de grups
    private BillCardAdapter.OnClickListener mOnClickListener; // Qui hagi de repintar la ReciclerView
    // quan s'amagui
    private BillCardAdapter.OnLongClickListener mOnLongClickListener;
    private BillCardAdapter.OnClickDetailsListener mOnClickDetailsListener; // Qui hagi de repintar la ReciclerView
    // quan s'amagui


    // Constructor
    public BillCardAdapter(ArrayList<Bill> userList) {
        this.mBills = userList; // no fa new (La llista la manté el ViewModel)

    }

    public void setOnClickListener(BillCardAdapter.OnClickListener listener) {
        this.mOnClickListener = listener;
    }
    public void setOnLongClickListener(BillCardAdapter.OnLongClickListener listener) {
        this.mOnLongClickListener = listener;
    }
    public void setOnClickDetailsListener(BillCardAdapter.OnClickDetailsListener listener) {
        this.mOnClickDetailsListener = listener;
    }


    @NonNull
    @Override
    public BillCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.bill_card_layout, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new BillCardAdapter.ViewHolder(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull BillCardAdapter.ViewHolder holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mBills.get(position), this.mOnClickListener, this.mOnClickDetailsListener, this.mOnLongClickListener);
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
        private final TextView mCardDate;
        private final TextView mCardHour;
        private final TextView mCardPayer;
        private final ImageView mCardImage;
        private final Button mCardDetails;

        private final MaterialCardView mCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardName = itemView.findViewById(R.id.billNameText);
            mCardPrice = itemView.findViewById(R.id.billPriceText);
            mCardDate = itemView.findViewById(R.id.billDateText);
            mCardHour = itemView.findViewById(R.id.billTimeText);
            mCardPayer = itemView.findViewById(R.id.billPayerText);
            mCardDetails = itemView.findViewById(R.id.billDetailsButton);
            mCardImage = itemView.findViewById(R.id.groupDescTextView);
            mCard = itemView.findViewById(R.id.cardBill);
        }

        public void bind(final Bill bill, BillCardAdapter.OnClickListener listener, BillCardAdapter.OnClickDetailsListener listenerDetails, BillCardAdapter.OnLongClickListener longListener) { //poner foto y textos
            mCardName.setText(bill.getName());
            if(bill.isSettled()){
                mCardName.setTextColor(Color.parseColor("#1AFF00"));
            } else {
                mCardName.setTextColor(Color.parseColor("#FF0000"));
            }
            mCardPrice.setText(String.valueOf(bill.getPrice()) + " €");
            mCardDate.setText(bill.getDate());
            mCardHour.setText(bill.getHour());
            mCardPayer.setText("Pagó: " + UserViewModel.getContact(bill.getPayerName()));
            if (bill.getPictureUrl()!=null && !bill.getPictureUrl().equals("")) {
                Picasso.get().load(bill.getPictureUrl()).into(mCardImage);
            }
            else
                Picasso.get().load(R.drawable.logo).into(mCardImage);
            // Seteja el listener onClick del botó d'amagar (hide), que alhora
            // cridi el mètode OnClickHide que implementen els nostres propis
            // listeners de tipus OnClickHideListener.

            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnClick(getAdapterPosition(), view);
                }
            });

            mCardDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerDetails.OnClickDetails(getAdapterPosition(), bill.getID(), bill);
                }
            });
            mCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longListener.OnLongClick(bill.getID(), getAdapterPosition(), bill);
                    return false;
                }
            });

        }
    }

}

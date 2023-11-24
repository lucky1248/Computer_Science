package ub.edu.moneysplitter.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Bill;
import ub.edu.moneysplitter.model.Group;

public class NewBillsDialogCardAdapter extends RecyclerView.Adapter<NewBillsDialogCardAdapter.ViewHolderDialog>{




    public interface OnClickListener {
        void OnClick(String billID, int position, Bill bill);
    }


    private ArrayList<Bill> mBills; // Referència a la llista de grups
    private NewBillsDialogCardAdapter.OnClickListener mOnClickListener; // Qui hagi de repintar la ReciclerView


    public interface OnClickAcceptListener {
        void OnClick(String billID, int position, Bill bill);
    }


    private NewBillsDialogCardAdapter.OnClickAcceptListener mOnClickAcceptListener; // Qui hagi de repintar la ReciclerView




    //
    // quan s'amagui


    // Constructor
    public NewBillsDialogCardAdapter(ArrayList<Bill> userList) {
        this.mBills = userList; // no fa new (La llista la manté el ViewModel)

    }

    public void setOnClickAcceptListener(NewBillsDialogCardAdapter.OnClickAcceptListener listener) {
        this.mOnClickAcceptListener = listener;
    }




    @NonNull
    @Override
    public NewBillsDialogCardAdapter.ViewHolderDialog onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.new_bill_dialog_card_layout, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new NewBillsDialogCardAdapter.ViewHolderDialog(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull NewBillsDialogCardAdapter.ViewHolderDialog holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mBills.get(position), this.mOnClickListener, this.mOnClickAcceptListener);
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {
        return mBills.size();
    }

    public static class ViewHolderDialog extends RecyclerView.ViewHolder {
        private final TextView mBillName;
        private final TextView mGroupName;

        private final ImageButton mAcceptBill;

        private final MaterialCardView mCard;

        public ViewHolderDialog(@NonNull View itemView) {
            super(itemView);
            mBillName = itemView.findViewById(R.id.newBillName);
            mGroupName = itemView.findViewById(R.id.newBillGroupName);
            mAcceptBill = itemView.findViewById(R.id.acceptNewBillBtn);
            mCard = itemView.findViewById(R.id.cardNewBills);
        }

        public void bind(final Bill bill, NewBillsDialogCardAdapter.OnClickListener listener, NewBillsDialogCardAdapter.OnClickAcceptListener listenerAcc) { //poner foto y textos
            mBillName.setText(bill.getName());
            mGroupName.setText(bill.getGroupName());

            // Carrega foto de l'usuari de la llista directament des d'una Url
            // d'Internet.
            // Seteja el listener onClick del botó d'amagar (hide), que alhora
            // cridi el mètode OnClickHide que implementen els nostres propis
            // listeners de tipus OnClickHideListener.

            /*mCardPictureUrl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener2.OnClickPicture(getAdapterPosition());
                }
            });*/



/*            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnClick(bill.getID(), getAdapterPosition(), bill);
                }
            });*/

            mAcceptBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerAcc.OnClick(bill.getID(), getAdapterPosition(), bill);
                }
            });
        }
    }

    public void deleteItem(int position) {

        this.mBills.remove(position);
        notifyItemRemoved(position);
    }
}

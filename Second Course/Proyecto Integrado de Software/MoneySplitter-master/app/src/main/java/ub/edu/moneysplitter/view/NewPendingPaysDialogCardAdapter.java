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
import ub.edu.moneysplitter.model.MemberDebt;
import ub.edu.moneysplitter.model.UserContactsLocal;

public class NewPendingPaysDialogCardAdapter extends RecyclerView.Adapter<NewPendingPaysDialogCardAdapter.ViewHolderDialog>{


    public ArrayList<MemberDebt> getList() {
        return mDebts;
    }

    public void setItemsArrayList(ArrayList<MemberDebt> users) {
        this.mDebts = users;
    }

    public interface OnClickListener {
        void OnClick(String debtID, int position, MemberDebt md);
    }


    private ArrayList<MemberDebt> mDebts; // Referència a la llista del deute d'un membere
    private NewPendingPaysDialogCardAdapter.OnClickListener mOnClickListener; // Qui hagi de repintar la ReciclerView


    public interface OnClickAcceptListener {
        void OnClick(String debtID, int position, MemberDebt md);
    }


    private NewPendingPaysDialogCardAdapter.OnClickAcceptListener mOnClickAcceptListener; // Qui hagi de repintar la ReciclerView




    //
    // quan s'amagui


    // Constructor
    public NewPendingPaysDialogCardAdapter(ArrayList<MemberDebt> userList) {
        this.mDebts = userList; // no fa new (La llista la manté el ViewModel)

    }

    public void setOnClickAcceptListener(NewPendingPaysDialogCardAdapter.OnClickAcceptListener listener) {
        this.mOnClickAcceptListener = listener;
    }




    @NonNull
    @Override
    public NewPendingPaysDialogCardAdapter.ViewHolderDialog onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.new_debt_dialog_card_layout, parent, false); //le pasociamos al padre


        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new NewPendingPaysDialogCardAdapter.ViewHolderDialog(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull NewPendingPaysDialogCardAdapter.ViewHolderDialog holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mDebts.get(position), this.mOnClickListener, this.mOnClickAcceptListener);
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {

        return mDebts.size();
    }

    public static class ViewHolderDialog extends RecyclerView.ViewHolder {
        private final TextView mDebtPrice;

        private final ImageButton mDebtSettled;

        private final MaterialCardView mCard;

        public ViewHolderDialog(@NonNull View itemView) {
            super(itemView);
            mDebtPrice = itemView.findViewById(R.id.newBillName);
            mDebtSettled = itemView.findViewById(R.id.acceptNewBillBtn);
            mCard = itemView.findViewById(R.id.cardNewBills);

        }

        public void bind(final MemberDebt md, NewPendingPaysDialogCardAdapter.OnClickListener listener, NewPendingPaysDialogCardAdapter.OnClickAcceptListener listenerAcc) { //poner foto y textos
            mDebtPrice.setText(md.getUserDebt() + " € a " + UserContactsLocal.getInstance().getContact(md.getDestUserID()));



            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnClick(md.getDebtID(), getAdapterPosition(), md);
                }
            });

            mDebtSettled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerAcc.OnClick(md.getDebtID(), getAdapterPosition(), md);
                }
            });
        }
    }

    public void deleteItem(int position) {
        MemberDebt g = this.mDebts.get(position);

        this.mDebts.remove(position);
        notifyItemRemoved(position);
    }
}

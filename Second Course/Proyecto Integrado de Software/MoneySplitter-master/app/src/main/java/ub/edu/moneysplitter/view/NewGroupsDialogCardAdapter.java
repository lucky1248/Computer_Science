package ub.edu.moneysplitter.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;

public class NewGroupsDialogCardAdapter extends RecyclerView.Adapter<NewGroupsDialogCardAdapter.ViewHolderDialog>{




    public interface OnClickListener {
        void OnClick(String groupID, int position, Group group);
    }


    private ArrayList<Group> mGroups; // Referència a la llista de grups
    private NewGroupsDialogCardAdapter.OnClickListener mOnClickListener; // Qui hagi de repintar la ReciclerView


    public interface OnClickAcceptListener {
        void OnClick(String groupID, int position, Group group);
    }


    private NewGroupsDialogCardAdapter.OnClickAcceptListener mOnClickAcceptListener; // Qui hagi de repintar la ReciclerView



    public interface OnClickDeclineListener {
        void OnClick(String groupID, int position, Group group);
    }


    private NewGroupsDialogCardAdapter.OnClickDeclineListener mOnClickDeclineListener; // Qui hagi de repintar la ReciclerView

    //
    // quan s'amagui


    // Constructor
    public NewGroupsDialogCardAdapter(ArrayList<Group> userList) {
        this.mGroups = userList; // no fa new (La llista la manté el ViewModel)

    }

    public void setOnClickAcceptListener(NewGroupsDialogCardAdapter.OnClickAcceptListener listener) {
        this.mOnClickAcceptListener = listener;
    }

    public void setOnClickDeclineListener(NewGroupsDialogCardAdapter.OnClickDeclineListener listener) {
        this.mOnClickDeclineListener = listener;
    }



    @NonNull
    @Override
    public NewGroupsDialogCardAdapter.ViewHolderDialog onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {



        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.new_group_dialog_card_layout, parent, false); //le pasociamos al padre


        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new NewGroupsDialogCardAdapter.ViewHolderDialog(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull NewGroupsDialogCardAdapter.ViewHolderDialog holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).

        holder.bind(mGroups.get(position), this.mOnClickListener, this.mOnClickAcceptListener, this.mOnClickDeclineListener);
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public static class ViewHolderDialog extends RecyclerView.ViewHolder {
        private final TextView mCardName;
        private final ImageButton mDeclineGroup;
        private final ImageButton mAcceptGroup;

        private final MaterialCardView mCard;

        public ViewHolderDialog(@NonNull View itemView) {
            super(itemView);
            mCardName = itemView.findViewById(R.id.newGroupName);
            mDeclineGroup = itemView.findViewById(R.id.declineNewGroupBtn);
            mAcceptGroup = itemView.findViewById(R.id.acceptNewGroupBtn);
            mCard = itemView.findViewById(R.id.cardNewGroups);
        }

        public void bind(final Group group, NewGroupsDialogCardAdapter.OnClickListener listener, NewGroupsDialogCardAdapter.OnClickAcceptListener listenerAcc, NewGroupsDialogCardAdapter.OnClickDeclineListener listenerDec) { //poner foto y textos
            mCardName.setText(group.getName());

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



            mCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnClick(group.getID(), getAdapterPosition(), group);
                }
            });

            mAcceptGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerAcc.OnClick(group.getID(), getAdapterPosition(), group);
                }
            });
            mDeclineGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerDec.OnClick(group.getID(), getAdapterPosition(), group);

                }
            });

        }
    }

    public void deleteItem(int position) {
        Group g = this.mGroups.get(position);

        this.mGroups.remove(position);
        notifyItemRemoved(position);
    }
}

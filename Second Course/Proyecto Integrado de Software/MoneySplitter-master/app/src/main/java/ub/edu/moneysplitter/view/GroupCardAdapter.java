package ub.edu.moneysplitter.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ub.edu.moneysplitter.R;
import ub.edu.moneysplitter.model.Group;


public class GroupCardAdapter extends RecyclerView.Adapter<GroupCardAdapter.ViewHolder>{




    public interface OnClickListener {
        void OnClick(String groupID, int position, Group group);
    }

    public interface OnLongClickListener {
        void OnLongClick(String groupID, int position, Group group);
    }


    private ArrayList<Group> mGroups; // Referència a la llista de grups
    private OnClickListener mOnClickListener; // Qui hagi de repintar la ReciclerView
    // quan s'amagui

    private OnLongClickListener mOnLongClickListener;

    // Constructor
    public GroupCardAdapter(ArrayList<Group> userList) {
        this.mGroups = userList; // no fa new (La llista la manté el ViewModel)

    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }
    public void setOnLongClickListener(OnLongClickListener listener) {
        this.mOnLongClickListener = listener;
    }


    public void setGroups(ArrayList<Group> groups) {
        this.mGroups = groups;
    }
    @NonNull
    @Override
    public GroupCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //parent.removeAllViews();
        // Inflate crea una view genèrica definida pel layout que l'hi passem (l'user_card_layout)
        View view = inflater.inflate(R.layout.group_card_layout, parent, false); //le pasociamos al padre

        // La classe ViewHolder farà de pont entre la classe User del model i la view (UserCard).
        return new GroupCardAdapter.ViewHolder(view);
    }

    /* Mètode cridat per cada ViewHolder de la RecyclerView */
    @Override
    public void onBindViewHolder(@NonNull GroupCardAdapter.ViewHolder holder, int position) {
        // El ViewHolder té el mètode que s'encarrega de llegir els atributs del Grup (1r parametre),
        // i assignar-los a les variables del ViewHolder.
        // Qualsevol listener que volguem posar a un item, ha d'entrar com a paràmetre extra (2n).
        holder.bind(mGroups.get(position), this.mOnClickListener, this.mOnLongClickListener);
    }

    /**
     * Retorna el número d'elements a la llista.
     * @return
     */
    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView mCardPictureUrl;
        private final TextView mCardName;
        private final TextView mCardDesc;
        private final TextView mCardDate;
        private final MaterialCardView mCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCardPictureUrl = itemView.findViewById(R.id.groupDescTextView);
            mCardName = itemView.findViewById(R.id.groupNameText);
            mCardDesc = itemView.findViewById(R.id.groupDescText);
            mCardDate = itemView.findViewById(R.id.groupDateText);
            mCard = itemView.findViewById(R.id.card);
        }

        public void bind(final Group group, OnClickListener listener, OnLongClickListener longListener) { //poner foto y textos
            mCardName.setText(group.getName());
            mCardDesc.setText(group.getDescription());
            mCardDate.setText(group.getDate());
            // Carrega foto de l'usuari de la llista directament des d'una Url
            // d'Internet.
            if (group.getURL()!=null && !group.getURL().equals("")) {
                Picasso.get().load(group.getURL()).into(mCardPictureUrl);
            }
            else{
                Picasso.get().load(R.drawable.logo).into(mCardPictureUrl);
            }
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

            mCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longListener.OnLongClick(group.getID(), getAdapterPosition(), group);
                    return false;
                }
            });

        }
    }
}

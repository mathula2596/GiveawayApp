package com.mis.givewayapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CardRecycleViewAdaptor extends RecyclerView.Adapter<CardRecycleViewAdaptor.CardViewHolder> {

    private Context context;
    private ArrayList<CardView> cardViewArrayList;
    private RecyclerViewClickListener listener;

    public CardRecycleViewAdaptor(Context context, ArrayList<CardView> cardViewArrayList,
                                  RecyclerViewClickListener listener) {
        this.context = context;
        this.cardViewArrayList = cardViewArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view,parent,false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        CardView cardView = cardViewArrayList.get(position);
        holder.setDetails(cardView);

    }

    @Override
    public int getItemCount() {
        return cardViewArrayList.size();
    }


    class CardViewHolder extends  RecyclerView.ViewHolder implements  View.OnClickListener{
        private TextView title,description,location,date,price;
        private ImageView image;

        CardViewHolder(View itemView){
            super(itemView);

            title = itemView.findViewById(R.id.product_title);
            description = itemView.findViewById(R.id.product_description);
            location = itemView.findViewById(R.id.product_location);
            price = itemView.findViewById(R.id.product_price);
            date = itemView.findViewById(R.id.product_created_date);
            image = itemView.findViewById(R.id.product_image);
            itemView.setOnClickListener(this);
        }

        void setDetails(CardView cardView)
        {
            title.setText(cardView.getTitle());
            description.setText(cardView.getDescription());
            location.setText(cardView.getLocation());
            price.setText(cardView.getPrice());
            date.setText(cardView.getDate());
            Picasso.get().load(cardView.getImage()).into(image);

        }

        @Override
        public void onClick(View view) {
            listener.onCLick(view,getAdapterPosition());
        }
    }

    public interface RecyclerViewClickListener{
        void onCLick(View v, int position);
    }
}

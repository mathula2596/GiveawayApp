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

import java.util.List;

public class RecycleViewAdaptor extends RecyclerView.Adapter<RecycleViewAdaptor.MyViewHolder>{

    Context context;
    List<ImageClass> imageClassList;

    public RecycleViewAdaptor(Context context, List<ImageClass> imageClassList) {
        this.context = context;
        this.imageClassList = imageClassList;
    }


    @NonNull
    @Override
    public RecycleViewAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.display_image, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecycleViewAdaptor.MyViewHolder holder, int position) {


        Picasso.get().load(imageClassList.get(position).getImage()).placeholder(R.drawable.display_image_bg).into(holder.imageView);


    }

    @Override
    public int getItemCount() {
        return imageClassList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.icon);
        }
    }

}

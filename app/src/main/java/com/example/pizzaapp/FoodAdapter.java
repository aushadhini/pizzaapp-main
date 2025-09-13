package com.example.pizzaapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.VH> {

    public interface OnFoodClickListener {
        void onItemClick(FoodItem item);
        void onLikeClick(FoodItem item);
    }

    private final List<FoodItem> data;
    private final OnFoodClickListener listener;

    public FoodAdapter(List<FoodItem> data, OnFoodClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_card, parent, false);  // your XML file
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        FoodItem item = data.get(position);

        h.img.setImageResource(item.imageRes);
        h.title.setText(item.title == null ? "" : item.title);
        h.subtitle.setText(item.subtitle == null ? "" : item.subtitle);
        h.rating.setText(" " + item.rating + " (" + item.reviews + ")");

        h.like.setImageResource(item.liked
                ? android.R.drawable.btn_star_big_on
                : R.drawable.ic_heart_outline_24);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(item); });
        h.like.setOnClickListener(v -> {
            if (listener != null) listener.onLikeClick(item);
            notifyItemChanged(h.getBindingAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView title, subtitle, rating;
        final ImageButton like;
        VH(@NonNull View itemView) {
            super(itemView);
            img      = itemView.findViewById(R.id.img);
            title    = itemView.findViewById(R.id.tvTitle);
            subtitle = itemView.findViewById(R.id.tvSubtitle);
            rating   = itemView.findViewById(R.id.tvRating);
            like     = itemView.findViewById(R.id.btnLike);
        }
    }
}

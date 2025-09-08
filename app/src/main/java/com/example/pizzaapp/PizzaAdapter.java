package com.example.pizzaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.button.MaterialButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PizzaAdapter extends RecyclerView.Adapter<PizzaAdapter.VH> {
    public interface OnPizzaClickListener {
        void onAddClicked(Pizza p, int pos);
        void onItemClicked(Pizza p, int pos);
    }
    private final Context ctx; private final List<Pizza> data; private final OnPizzaClickListener cb;
    public PizzaAdapter(Context c, List<Pizza> d, OnPizzaClickListener cb){ this.ctx=c; this.data=d; this.cb=cb; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_pizza_grid, parent, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Pizza p = data.get(pos);
        h.img.setImageResource(p.getImageRes());
        h.name.setText(p.getName());
        h.price.setText(p.getPrice());
        h.itemView.setOnClickListener(v -> { if(cb!=null) cb.onItemClicked(p,pos); });
        h.btnAdd.setOnClickListener(v -> { if(cb!=null) cb.onAddClicked(p,pos); });
    }
    @Override public int getItemCount(){ return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView name, price; MaterialButton btnAdd;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}

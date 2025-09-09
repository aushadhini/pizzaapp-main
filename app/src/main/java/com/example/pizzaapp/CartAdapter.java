package com.example.pizzaapp;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    public interface OnChanged {
        void onQtyChanged();
        void onItemRemoved();
    }
    private List<CartItem> data;
    private final OnChanged callback;

    public CartAdapter(List<CartItem> data, OnChanged cb) {
        this.data = data;
        this.callback = cb;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        CartItem it = data.get(pos);
        h.tvName.setText(it.getName());
        h.tvPrice.setText(String.format("LKR %.2f", it.getPrice()));
        h.tvQty.setText(String.valueOf(it.getQty()));
        h.tvLineTotal.setText(String.format("LKR %.2f", it.lineTotal()));
        // Load image if you have Glide/Picasso
        // Glide.with(h.img.getContext()).load(it.getImageUrl()).into(h.img);

        h.btnPlus.setOnClickListener(v -> {
            CartManager.get(v.getContext()).setQty(it.getId(), it.getQty() + 1);
            it.setQty(it.getQty() + 1);
            notifyItemChanged(h.getAdapterPosition());
            callback.onQtyChanged();
        });

        h.btnMinus.setOnClickListener(v -> {
            int newQty = it.getQty() - 1;
            CartManager.get(v.getContext()).setQty(it.getId(), newQty);
            if (newQty <= 0) {
                int p = h.getAdapterPosition();
                data.remove(p);
                notifyItemRemoved(p);
                callback.onItemRemoved();
            } else {
                it.setQty(newQty);
                notifyItemChanged(h.getAdapterPosition());
                callback.onQtyChanged();
            }
        });

        h.btnDelete.setOnClickListener(v -> {
            CartManager.get(v.getContext()).remove(it.getId());
            int p = h.getAdapterPosition();
            data.remove(p);
            notifyItemRemoved(p);
            callback.onItemRemoved();
        });
    }

    @Override public int getItemCount() { return data.size(); }
    public void submit(List<CartItem> newData) { this.data = newData; notifyDataSetChanged(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName, tvPrice, tvQty, tvLineTotal;
        ImageButton btnMinus, btnPlus, btnDelete;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.img);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvQty = v.findViewById(R.id.tvQty);
            tvLineTotal = v.findViewById(R.id.tvLineTotal);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

}


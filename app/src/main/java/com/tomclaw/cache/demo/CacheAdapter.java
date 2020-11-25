package com.tomclaw.cache.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CacheAdapter extends RecyclerView.Adapter<CacheAdapter.ViewHolder> {

    private final List<CacheItem> cacheItems;
    private final LayoutInflater inflater;
    private ItemClickListener clickListener;

    CacheAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.cacheItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.cache_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CacheItem item = cacheItems.get(position);
        holder.bindCacheItem(item);
    }

    @Override
    public int getItemCount() {
        return cacheItems.size();
    }

    void setCacheItems(List<CacheItem> cacheItems) {
        this.cacheItems.clear();
        this.cacheItems.addAll(cacheItems);
    }

    public interface ItemClickListener {

        void onItemClick(View view, int position);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView subtitle;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

        void bindCacheItem(CacheItem item) {
            title.setText(item.getKey());
            subtitle.setText(item.getSize());
        }

    }

    static class CacheItem {

        private final String key;
        private final String size;

        CacheItem(String key, String size) {
            this.key = key;
            this.size = size;
        }

        String getKey() {
            return key;
        }

        String getSize() {
            return size;
        }

    }

}

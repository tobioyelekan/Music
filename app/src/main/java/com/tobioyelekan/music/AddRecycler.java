package com.tobioyelekan.music;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 22/12/2017.
 */

public class AddRecycler extends RecyclerView.Adapter<AddRecycler.ViewHolder> {

    ArrayList<ArrayList<String>> datas;
    static OnClickListener listener;

    public AddRecycler(ArrayList<ArrayList<String>> datas) {
        this.datas = datas;
    }

    public void setOnItemClickListener(OnClickListener listener) {
        AddRecycler.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout click;
        TextView title;

        public ViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            click = (LinearLayout) v.findViewById(R.id.click);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.addto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ArrayList<String> data = datas.get(position);
        holder.title.setText(data.get(0));
        holder.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(data);
            }
        });
    }

    public interface OnClickListener {
        void onClick(ArrayList<String> data);
    }
}
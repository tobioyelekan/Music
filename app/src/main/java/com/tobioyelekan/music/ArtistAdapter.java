package com.tobioyelekan.music;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 15/11/2017.
 */

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {
    private static OnRvItemClickListener listener;
    private static OnOptionsClickListener listener2;

    Database myDb;

    public void setOnItemClickListener(OnRvItemClickListener listener) {
        ArtistAdapter.listener = listener;
    }

    public void setOnOptionsClickListener(OnOptionsClickListener listener) {
        ArtistAdapter.listener2 = listener;
    }

    ArrayList<ArrayList<String>> arrayList;
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView, more;
        TextView title, nosong;
        RelativeLayout container;

        public ViewHolder(View view) {
            super(view);
            imgView = (ImageView) view.findViewById(R.id.img);
            more = (ImageView) view.findViewById(R.id.more);
            title = (TextView) view.findViewById(R.id.title);
            nosong = (TextView) view.findViewById(R.id.nosong);
            container = (RelativeLayout) view.findViewById(R.id.container);
        }
    }

    public ArtistAdapter(ArrayList<ArrayList<String>> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
        myDb = new Database(context);
    }

    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ArrayList<String> obj = arrayList.get(position);
        final ViewHolder holder2 = holder;
//        holder.imgView.setImageResource(R.mipmap.albums);

//        if (!TextUtils.isEmpty(obj.get(3)))
//            Picasso.with(context).load(Uri.parse("file://" + obj.get(3))).placeholder(R.mipmap.albums)
//                    .error(R.mipmap.albums).noFade().into(holder.imgView);

        if (obj.get(0).equals("<unknown>")) {
            holder.title.setText("Unknown artist");
        } else {
            holder.title.setText(obj.get(0));
        }

        if (Integer.parseInt(obj.get(1)) > 1) {
            holder.nosong.setText(obj.get(1) + " songs");
        } else {
            holder.nosong.setText(obj.get(1) + " song");
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRvItemClickListener(obj);
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.play:
                                listener2.onOptionClick("play", obj, holder2.getAdapterPosition());
                                return true;
                            case R.id.share:
                                listener2.onOptionClick("share", obj, holder2.getAdapterPosition());
                                return true;
                            case R.id.delete:
                                listener2.onOptionClick("delete", obj, holder2.getAdapterPosition());
                                return true;
                        }
                        return false;
                    }
                });

                popup.inflate(R.menu.options2);
                popup.show();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface OnRvItemClickListener {
        void onRvItemClickListener(ArrayList<String> data);
    }

    public interface OnOptionsClickListener {
        void onOptionClick(String mode, ArrayList<String> data, int position);
    }
}

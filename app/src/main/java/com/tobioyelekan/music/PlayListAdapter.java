package com.tobioyelekan.music;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by TOBI OYELEKAN on 21/11/2017.
 */

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder> {

    private static OnItemClickListener listener;
    private static OnOptionsClickListener listener2;
    ArrayList<ArrayList<String>> datas;
    Context context;

    public PlayListAdapter(ArrayList<ArrayList<String>> data, Context context) {
        this.datas = data;
        this.context = context;
    }

    public void setOnClickListener(OnItemClickListener listener) {
        PlayListAdapter.listener = listener;
    }

    public void setOnOptionsClickListener(OnOptionsClickListener listener2) {
        PlayListAdapter.listener2 = listener2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img, more;
        TextView title, nosong;
        RelativeLayout container;

        public ViewHolder(View v) {
            super(v);
            img = (ImageView) v.findViewById(R.id.img);
            more = (ImageView) v.findViewById(R.id.more);
            title = (TextView) v.findViewById(R.id.title);
            nosong = (TextView) v.findViewById(R.id.nosong);
            container = (RelativeLayout) v.findViewById(R.id.container);
        }
    }

    @Override
    public PlayListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlayListAdapter.ViewHolder holder, int position) {
        final ArrayList<String> data = datas.get(position);
        final PlayListAdapter.ViewHolder holder2 = holder;

        if (position == 0) {
            holder.title.setText(data.get(0));
            holder.img.setImageResource(R.mipmap.add);
            holder.nosong.setVisibility(View.GONE);
            holder.more.setVisibility(View.GONE);
            setMargins(holder.title, 0, 20, 0, 0);
        } else {

            holder.nosong.setText(data.get(1));
            holder.img.setVisibility(View.VISIBLE);

            if (data.get(0).equalsIgnoreCase("Last added") || data.get(0).equalsIgnoreCase("Most Played")) {
//                Toast.makeText(context, data.get(1), Toast.LENGTH_SHORT).show();
                setMargins(holder.title, 0, 20, 0, 0);
                holder.more.setVisibility(View.GONE);
                holder.nosong.setVisibility(View.GONE);
                if (data.get(0).equalsIgnoreCase("Last added")) {
                    holder.title.setText("Recently added");
                    holder.img.setImageResource(R.mipmap.recent);
                } else if (data.get(0).equalsIgnoreCase("Most Played")) {
                    holder.title.setText(data.get(0));
                    holder.img.setImageResource(R.mipmap.most);
                }
            } else {
                holder.title.setText(data.get(0));

                if (data.get(0).equalsIgnoreCase("Favorites")) {
                    holder.img.setImageResource(R.mipmap.favlist);
                    holder.more.setVisibility(View.GONE);
                } else {
                    holder.img.setImageResource(R.mipmap.playlist1);
                    holder.more.setVisibility(View.VISIBLE);
                }
                holder.nosong.setVisibility(View.VISIBLE);

                if (data.get(1).equals("No songs")) {
                    holder.nosong.setText(data.get(1));
                } else {
                    if (Integer.parseInt(data.get(1)) > 1) {
                        holder.nosong.setText(data.get(1) + " songs");
                    } else {
                        holder.nosong.setText(data.get(1) + " song");
                    }
                }
            }
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(data, holder.getAdapterPosition());
            }
        });

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.inflate(R.menu.options);

                popup.getMenu().findItem(R.id.add).setVisible(false);
                popup.getMenu().findItem(R.id.set).setVisible(false);
                popup.getMenu().findItem(R.id.info).setVisible(false);
                popup.getMenu().findItem(R.id.remove).setVisible(false);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.play:
                                Toast.makeText(context, "play", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.share:
                                Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.delete:
                                if (delete(data.get(2)) > 0) {
                                    datas.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.rename:
                                listener2.onOptionClick("rename", holder2.getAdapterPosition(), data);
                                return true;
                        }
                        return false;
                    }
                });

                popup.show();
            }
        });

    }

    private int delete(String id) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, Long.parseLong(id));
        return contentResolver.delete(uri, null, null);
    }

    private void setMargins(View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public interface OnItemClickListener {
        void onClick(ArrayList<String> data, int positon);
    }

    public interface OnOptionsClickListener {
        void onOptionClick(String mode, int position, ArrayList<String> data);
    }

}

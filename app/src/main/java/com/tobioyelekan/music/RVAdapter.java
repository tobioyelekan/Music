package com.tobioyelekan.music;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Locale;

/**
 * Created by TOBI OYELEKAN on 20/06/2017.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> {
    private static OnRvItemClickListener listener;
    private static OnOptionClickListener listener2;
    private static OnItemChangeListener listener3;

    Database myDb;
    ArrayList<ArrayList<String>> arrayList;
    Context context;
    String playlist;

    public void setOnItemClickListener(OnRvItemClickListener listener) {
        RVAdapter.listener = listener;
    }

    public void setOnOptionClickListener(OnOptionClickListener listener) {
        RVAdapter.listener2 = listener;
    }

    public void setOnItemChangeListener(OnItemChangeListener listener) {
        RVAdapter.listener3 = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgView, musicDet, imgFav;
        TextView title, artist, duration;
        RelativeLayout container;

        public ViewHolder(View view) {
            super(view);
            imgView = (ImageView) view.findViewById(R.id.img);
            imgFav = (ImageView) view.findViewById(R.id.fav);
            title = (TextView) view.findViewById(R.id.title);
            artist = (TextView) view.findViewById(R.id.artist);
            duration = (TextView) view.findViewById(R.id.dur);
            musicDet = (ImageView) view.findViewById(R.id.musicDet);
            container = (RelativeLayout) view.findViewById(R.id.container);
        }
    }

    public RVAdapter(ArrayList<ArrayList<String>> arrayList, Context context, String playlist) {
        this.arrayList = arrayList;
        this.context = context;
        myDb = new Database(context);
        this.playlist = playlist;
    }

    @Override
    public RVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listlayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final ArrayList<String> obj = arrayList.get(position);

        holder.title.setText(obj.get(0));

        if (obj.get(1).equals("<unknown>")) {
            holder.artist.setText("Unknwon artist");
        } else {
            holder.artist.setText(obj.get(1));
        }
        holder.duration.setText(obj.get(2));

        if (exist("favorites", obj.get(4)))
            holder.imgFav.setImageResource(R.mipmap.fav);
        else
            holder.imgFav.setImageResource(R.mipmap.addfav);

        holder.musicDet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.inflate(R.menu.options);

                popup.getMenu().findItem(R.id.play).setVisible(false);
                popup.getMenu().findItem(R.id.remove).setVisible(false);
                popup.getMenu().findItem(R.id.rename).setVisible(false);

                if (playlist.equals("Recently added") || playlist.equals("Last Added") || playlist.equals("Most Played") || playlist.equals("artist") || playlist.equals("album")) {
                    popup.getMenu().findItem(R.id.delete).setVisible(false);
                } else if (playlist.equals("all")) {

                } else {
                    popup.getMenu().findItem(R.id.remove).setVisible(true);
                    popup.getMenu().findItem(R.id.delete).setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.add:
                                listener2.onOptionClick("add", obj);
                                return true;
                            case R.id.set:
                                listener2.onOptionClick("set", obj);
                                return true;
                            case R.id.share:
                                listener2.onOptionClick("share", obj);
                                return true;
                            case R.id.delete:
                                if (removeFromPhone(obj.get(4)) > 0) {
                                    arrayList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    Toast.makeText(context, "music deleted", Toast.LENGTH_SHORT).show();
                                }
                                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show();
                                return true;
                            case R.id.remove:
                                if (remove(playlist, obj.get(4)) > 0) {
                                    arrayList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    Toast.makeText(context, "removed", Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            case R.id.info:
                                listener2.onOptionClick("info", obj);
                                return true;
                        }
                        return false;
                    }
                });

                popup.show();
            }
        });

        holder.imgFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exist("favorites", obj.get(4))) {
                    if (playlist.equalsIgnoreCase("favorites")) {
                        if (remove("favorites", obj.get(4)) > 0) {
                            arrayList.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            Toast.makeText(context, "removed from fav", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (remove("favorites", obj.get(4)) > 0) {
                            Toast.makeText(context, "removed from fav", Toast.LENGTH_SHORT).show();
                            ((ImageView) v).setImageResource(R.mipmap.addfav);
                        }
                    }
                } else {
                    addToFav(Integer.parseInt(obj.get(4)), v);
                }
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRvItemClickListener(obj, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public interface OnRvItemClickListener {
        void onRvItemClickListener(ArrayList<String> data, int postion);
    }

    public interface OnOptionClickListener {
        void onOptionClick(String mode, ArrayList<String> data);
    }

    public interface OnItemChangeListener {
        void onItemChangeListener(int size);
    }

    private void addToFav(int audio_id, View v) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", getId("favorites"));
        Cursor cursor = resolver.query(uri, new String[]{MediaStore.Audio.Playlists.Members.PLAY_ORDER}, null, null, null);
        int base = 0;
        if (cursor != null) {
            if (cursor.moveToLast())
                base = cursor.getInt(0);
            cursor.close();

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + 1);
            contentValues.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio_id);
            resolver.insert(uri, contentValues);

            Toast.makeText(context, "added to fav", Toast.LENGTH_SHORT).show();
            ((ImageView) v).setImageResource(R.mipmap.fav);
            notifyDataSetChanged();
        }
    }

    private int remove(String playlist, String audioId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", getId(playlist));
        return resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{audioId});
    }

    private boolean exist(String playlist_name, String audio_id) {
        boolean exist = false;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", getId(playlist_name));
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Playlists.Members.DISPLAY_NAME},
                MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", new String[]{audio_id}, null);

        if (cursor != null) {
            exist = cursor.getCount() > 0;
            cursor.close();
        }

        return exist;
    }

    private long getId(String playlist_name) {
        long id = -1;
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.Audio.Playlists._ID},
                MediaStore.Audio.Playlists.NAME + "=?", new String[]{playlist_name}, null);
        if (cursor != null) {
            if (cursor.moveToNext())
                id = cursor.getLong(0);
            cursor.close();
        }

        return id;
    }

    private int removeFromPhone(String audioId) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        return resolver.delete(uri, MediaStore.Audio.Media._ID + "=?", new String[]{audioId});
    }
}
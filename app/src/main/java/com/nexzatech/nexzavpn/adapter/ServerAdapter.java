package com.nexzatech.nexzavpn.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexzatech.nexzavpn.R;
import com.nexzatech.nexzavpn.data.model.Server;

import java.util.List;

public class ServerAdapter extends BaseAdapter {

    private final Context context;
    private final List<Server> serverList;
    private final LayoutInflater inflater;

    public ServerAdapter(Context context, List<Server> serverList) {
        this.context = context;
        this.serverList = serverList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return serverList.size();
    }

    @Override
    public Server getItem(int position) {
        return serverList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {

            convertView = inflater.inflate(
                    R.layout.server_card,
                    parent,
                    false
            );

            holder = new ViewHolder();

            holder.ivServerFlag =
                    convertView.findViewById(R.id.ivServerFlag);

            holder.tvCountryName =
                    convertView.findViewById(R.id.tvCountryName);

            holder.tvServerName =
                    convertView.findViewById(R.id.tvServerName);

            holder.ivServerSelected =
                    convertView.findViewById(R.id.ivServerSelected);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        Server server = getItem(position);

        holder.tvCountryName.setText(server.getCountry());
        holder.tvServerName.setText(server.getName());

        // API response-e flag URL nei.
        // Tai apatoto local flag image set korchi na.

        holder.ivServerSelected.setImageResource(
                R.drawable.ic_chevron_right
        );

        return convertView;
    }

    private static class ViewHolder {

        ImageView ivServerFlag;
        TextView tvCountryName;
        TextView tvServerName;
        ImageView ivServerSelected;
    }
}
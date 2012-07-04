
package com.inc.im.serptracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inc.im.serptracker.R;

public class RawDataAdapter extends BaseAdapter {

    private String[] list;
    private LayoutInflater mInflater;
    private int rank;

    public RawDataAdapter(String[] list, Context con, int rankNumber) {
        this.list = list;
        mInflater = LayoutInflater.from(con);
        rank = rankNumber;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list[position];
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        // if (convertView == null) {
        convertView = mInflater.inflate(R.layout.raw_data_list_item, null);
        viewHolder = new ViewHolder();

        viewHolder.number = (TextView) convertView.findViewById(R.id.TextViewNumber);
        viewHolder.url = (TextView) convertView.findViewById(R.id.TextView);

        convertView.setTag(viewHolder);
        // }
        // else {
        // viewHolder = (ViewHolder) convertView.getTag();
        // }

        if (position == rank) {
            convertView.setBackgroundResource(R.drawable.ok_button);
        }

        viewHolder.number.setText(position + 1 + "");

        String selectedUrl = list[position];
        if (selectedUrl.length() > 45)
            viewHolder.url.setText(selectedUrl.subSequence(0, 45) + "...");
        else
            viewHolder.url.setText(list[position]);

        return convertView;
    }

    private class ViewHolder {
        TextView number;
        TextView url;

    }

}

package com.inc.im.serptracker.adapters;

import java.util.ArrayList;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.Keyword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivityListAdapter extends BaseAdapter {

	private Context con;
	private ArrayList<Keyword> input;

	public MainActivityListAdapter(Context con, ArrayList<Keyword> input) {
		this.con = con;
		this.input = input;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return input.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// inflate from layout
		// bind values

		if (convertView == null) {
			// convertView = new

			LayoutInflater inf = (LayoutInflater) con
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = inf.inflate(R.layout.main_activity_listview_item,
					null);
		}

		Keyword k = input.get(position);

		TextView tvKeyword = (TextView) convertView
				.findViewById(R.id.textView1);
		TextView tvRank = (TextView) convertView.findViewById(R.id.textView2);
		TextView tvRankOld = (TextView) convertView
				.findViewById(R.id.textView3);

		String keyword = k.keyword;
		String newRank = Integer.toString(k.newRank);
		String oldRank = Integer.toString(k.oldRank);

		tvKeyword.setText(keyword);
		tvRank.setText(newRank);
		tvRankOld.setText(oldRank);

		return convertView;
	}
}

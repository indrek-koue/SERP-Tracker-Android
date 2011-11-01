package com.inc.im.serptracker.adapters;

import java.util.ArrayList;

import com.inc.im.serptracker.R;
import com.inc.im.serptracker.data.Keyword;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

		View v = convertView;

		if (v == null) {
			// convertView = new

			LayoutInflater inf = (LayoutInflater) con
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			v = inf.inflate(R.layout.main_activity_listview_item, null);
		}

		Keyword k = input.get(position);

		TextView tvKeyword = (TextView) v.findViewById(R.id.textView1);
		TextView tvRank = (TextView) v.findViewById(R.id.textView2);
		TextView tvRankOld = (TextView) v.findViewById(R.id.textView3);

		String keyword = k.keyword;
		String newRank = Integer.toString(k.newRank);
		String oldRank = Integer.toString(k.oldRank);

		tvRankOld.setTextColor(R.color.dred);

		ImageView iv = (ImageView) v.findViewById(R.id.imageView1);

		if (k.newRank > k.oldRank) {
			iv.setBackgroundResource(R.drawable.down);
			tvRankOld.setVisibility(View.VISIBLE);
			tvRankOld.setTextColor(con.getResources().getColor(R.color.dred));
		} else if (k.newRank < k.oldRank) {
			iv.setBackgroundResource(R.drawable.up);
			tvRankOld.setVisibility(View.VISIBLE);
			tvRankOld.setTextColor(con.getResources().getColor(R.color.green));
		} else {
			iv.setVisibility(View.GONE);
			tvRankOld.setVisibility(View.GONE);
		}

		tvKeyword.setText(keyword);
		tvRank.setText(newRank);
		tvRankOld.setText(oldRank);

		// special cases
		// 0 - empty field in DB - new
		// -1 - not ranked in top 100
		// -2 - error getting data

		if (k.newRank == 0) {
			if (k.oldRank == 0) {

				// just added
				tvRank.setVisibility(View.GONE);
				iv.setVisibility(View.GONE);
				tvRankOld.setVisibility(View.GONE);
			} else {
				// has been used before (has oldrank) and started from
				// mainscreen first time
				tvRank.setText(oldRank);
				iv.setVisibility(View.GONE);
				tvRankOld.setVisibility(View.GONE);

				if (k.oldRank == -1) {
					iv.setVisibility(View.GONE);
					tvRankOld.setVisibility(View.GONE);
					tvRank.setText("-");
				}

				if (k.oldRank == -2) {
					// hide image + old rank
					iv.setVisibility(View.GONE);
					tvRankOld.setVisibility(View.GONE);

					// display error msg
					tvRank.setText("error");
					tvRank.setTextColor(con.getResources().getColor(
							R.color.dred));
				}
			}

		}

		if (k.newRank == -1) {
			iv.setVisibility(View.GONE);
			tvRankOld.setVisibility(View.GONE);
			tvRank.setText("-");
		}

		if (k.newRank == -2) {
			// hide image + old rank
			iv.setVisibility(View.GONE);
			tvRankOld.setVisibility(View.GONE);

			// display error msg
			tvRank.setText("error");
			tvRank.setTextColor(con.getResources().getColor(R.color.dred));
		}

		return v;
	}
}

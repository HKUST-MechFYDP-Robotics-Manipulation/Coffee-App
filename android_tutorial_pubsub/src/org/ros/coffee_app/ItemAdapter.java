package org.ros.coffee_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ros.android.android_tutorial_pubsub.R;

/**
 * Created by shan on 18年2月21日.
 */

public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    String[] items;
    String[] prices;
    int[] coffeepic;

    public ItemAdapter(Context c, String[] i, String[] p, String[] d,int[] co){
        items = i;
        prices = p;
        coffeepic = co;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = mInflater.inflate(R.layout.my_listview_detail,null);
        TextView nameTextView = (TextView) v.findViewById(R.id.nameTextView);
        TextView priceTextView = (TextView) v.findViewById(R.id.priceTextView);
        ImageView img = (ImageView) v.findViewById(R.id.img);

        String name = items[position];
        String cost = prices[position];
        int image = coffeepic [position];

        nameTextView.setText(name);
        priceTextView.setText(cost);
        img.setImageResource(image);
        return v;
    }
}

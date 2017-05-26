package com.np.ioc_sample;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.np.annotation.BindView;
import com.np.ioc.ViewInjector;

import java.util.List;

public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private List<String> mDataList;
    private int layoutId;

    public MyAdapter(Context context, List<String> dataList, int item) {
        this.mContext = context;
        this.mDataList = dataList;
        this.layoutId = item;
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvItem.setText(mDataList.get(position));
        return convertView;
    }

     static class ViewHolder {
        @BindView(R.id.item_tv)
        TextView tvItem;
        ViewHolder(View view) {
            ViewInjector.injectView(this, view);
        }
    }
}

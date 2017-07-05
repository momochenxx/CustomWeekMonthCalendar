package com.momochen.customweekmonthcalendar.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.momochen.customweekmonthcalendar.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by momochen on 2017/7/5.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ContentViewHolder> {

    Activity activity;
    List<String> datas;

    public MainAdapter(Activity activity) {
        this.activity = activity;
        datas = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            datas.add("momochen" + i);
        }
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContentViewHolder(activity.getLayoutInflater().inflate(R.layout.item_text, null, false));
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, int position) {
        holder.tvContent.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    protected class ContentViewHolder extends RecyclerView.ViewHolder {

        protected TextView tvContent;

        public ContentViewHolder(View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }

    }
}

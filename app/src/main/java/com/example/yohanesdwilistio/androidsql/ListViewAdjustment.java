package com.example.yohanesdwilistio.androidsql;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ListViewAdjustment {
    public void adjustListViewwithSimple(ListView list, SimpleAdapter adapter){
        list.setAdapter(adapter);
        int totalHeight = 0;
        ListAdapter listAdapter = list.getAdapter();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, list);
            listItem.measure(0, 0);
            totalHeight += (listItem.getMeasuredHeight() * 1.05);
        }
        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = totalHeight + (list.getDividerHeight() * (listAdapter.getCount()));
        list.setLayoutParams(params);
    }
}

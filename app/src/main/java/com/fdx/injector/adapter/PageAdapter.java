package com.fdx.injector.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.fdx.injector.R;

public class PageAdapter extends PagerAdapter {
    private final Activity activity;

    public PageAdapter(Activity activity) {
        this.activity = activity;
    }
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        int[] views = {R.id.tab1, R.id.tab2 , R.id.tab3};

        return activity.findViewById(views[position]);
    }
}

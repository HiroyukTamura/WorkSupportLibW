/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cks.hiroyuki2.worksupprotlib.Util;
import com.example.hiroyuki3.worksupportlibw.Presenter.AnalyticsVPUiOperator;
import com.example.hiroyuki3.worksupportlibw.R;

import org.jetbrains.annotations.Contract;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TreeMap;

import static com.cks.hiroyuki2.worksupprotlib.Util.PREF_NAME;

/**
 * Analytics画面のVPAdapterおじさん！でもUIの操作AnalyticsVPUiOperatorにおまかせ
 * AnalyticsFragment
 */
public class AnalyticsVPAdapter extends PagerAdapter {
    private static final String TAG = "MANUAL_TAG: " + AnalyticsVPAdapter.class.getSimpleName();
    public static final int PAGE = 25;
    public static final int OFFSET = 2;
    private LayoutInflater inflater;
    private Context context;
    private Calendar startCal;
    private TreeMap<Integer, AnalyticsVPUiOperator> operators = new TreeMap<>();
    private Fragment analyticsFragment;
    private String uid;
    private int toolbarHeight;

    public AnalyticsVPAdapter(Context context, Fragment analyticsFragment, String uid, int toolbarHeignt){
        this.context = context;
        this.startCal = makeStartCal(context);
        this.analyticsFragment = analyticsFragment;
        this.uid = uid;
        this.toolbarHeight = toolbarHeignt;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return PAGE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        operators.remove(position);
        container.removeView((View) object);
    }

    @Override @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View root =  inflater.inflate(R.layout.fragment_analytics, null);
        root.setTag(position);
        Calendar cal = getCal(position);
        Log.d(TAG, "instantiateItem: "+ cal.getTime().toString());
        AnalyticsVPUiOperator operator = new AnalyticsVPUiOperator(new WeakReference<>(root), cal, analyticsFragment, uid, toolbarHeight);
        operators.put(position, operator);
        container.addView(root);
        return root;
    }

    //カレンダーを週の頭へ
    private Calendar makeStartCal(Context context){
        Calendar startCal = Calendar.getInstance();
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int startDof = pref.getInt(Util.PREF_KEY_START_OF_WEEK, Calendar.SUNDAY);
        while (startCal.get(Calendar.DAY_OF_WEEK) != startDof){
            startCal.add(Calendar.DATE, -1);
        }
        return startCal;
    }

    private Calendar getCal(int position){
        Calendar cal = Util.getCopyOfCal(startCal);
        cal.add(Calendar.DATE, 7*(position - PAGE/2));
        return cal;
    }

    @Contract(pure = true)
    public TreeMap<Integer, AnalyticsVPUiOperator> getOperators(){
        return operators;
    }
}

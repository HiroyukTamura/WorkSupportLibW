/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupprotlib.FirebaseConnection;
import com.example.hiroyuki3.worksupportlibw.R;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static com.cks.hiroyuki2.worksupprotlib.Util.PREF_KEY_START_OF_WEEK;
import static com.cks.hiroyuki2.worksupprotlib.Util.PREF_NAME;
import static com.cks.hiroyuki2.worksupprotlib.Util.cal2date;
import static com.cks.hiroyuki2.worksupprotlib.Util.date2Cal;
import static com.cks.hiroyuki2.worksupprotlib.Util.datePattern;
import static com.cks.hiroyuki2.worksupprotlib.Util.getCopyOfCal;
import static com.cks.hiroyuki2.worksupprotlib.Util.logStackTrace;

/**
 * Record画面のTabを担うおじさん！
 */

public class RecordTabVPAdapter extends PagerAdapter implements View.OnClickListener{

    private static final String TAG = "MANUAL_TAG: " + RecordTabVPAdapter.class.getSimpleName();
//    private RecordFragment fragment;
    private Context context;
    private LayoutInflater inflater;
    private List<String> listDayOfWeek;
    private Calendar calMed;
    public final static int PAGE_NUM = 15;
    public final static int MED_NUM = PAGE_NUM/2;
    public final static String TAG_VISIBLE = "TAG_VISIBLE";
    public int startOfWeek;
    public View currentItem;
    private AdapterCallback callback;
    private List<String> holidays;

    public RecordTabVPAdapter(Context context, Calendar calMed, AdapterCallback callback){
//        this.fragment = fragment;
        this.context = context;
        this.calMed = calMed;
        this.callback = callback;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        startOfWeek =  pref.getInt(PREF_KEY_START_OF_WEEK, Calendar.SUNDAY);
        listDayOfWeek = makeWofList(startOfWeek);
        holidays = FirebaseConnection.getInstance().getHolidayArr();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return PAGE_NUM;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.record_tab_item, null);

        LinearLayout ll = view.findViewById(R.id.date_container);
        LinearLayout dayContainer = view.findViewById(R.id.day_container);
        int dayOfWeekMed = calMed.get(Calendar.DAY_OF_WEEK);
        Calendar calTmp = getCopyOfCal(calMed);
        calTmp.add(Calendar.DATE, 7*(- MED_NUM + position));
        calTmp.add(Calendar.DATE, -dayOfWeekMed + 1);
        int startDate = Integer.parseInt(cal2date(calTmp, datePattern));
        for (int i=0; i<7; i++){
            FrameLayout fm = (FrameLayout) ll.getChildAt(i);
            fm.setOnClickListener(this);
            fm.setTag(Integer.parseInt(cal2date(calTmp, datePattern)));
            TextView dayTv = fm.findViewById(R.id.tv);
            dayTv.setText(String.valueOf(calTmp.get(Calendar.DAY_OF_MONTH)));
            ImageView iv = fm.findViewById(R.id.iv);
            if (calMed.compareTo(calTmp) == 0){
                iv.setVisibility(View.VISIBLE);
                dayTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                iv.setTag(TAG_VISIBLE);
            }
            TextView tv = (TextView) dayContainer.getChildAt(i);
            tv.setText(listDayOfWeek.get(i));

            if (calTmp.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || holidays.contains((cal2date(calTmp, datePattern)))){
                tv.setTextColor(ContextCompat.getColor(context, R.color.red_anton));
                dayTv.setTextColor(ContextCompat.getColor(context, R.color.red_anton));
            }
            calTmp.add(Calendar.DATE, 1);
        }

        Log.d(TAG, "instantiateItem: " + startDate);
        view.setTag(startDate);
        container.addView(view);
        return view;
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        currentItem = (View) object;
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public void onClick(View view) {
        View oldCircle = currentItem.findViewWithTag(TAG_VISIBLE);
        if (oldCircle != null){
            String dateStr = Integer.toString((Integer) view.getTag());
            oldCircle.setTag(null);
            oldCircle.setVisibility(View.GONE);
            TextView oldTv = ((FrameLayout)oldCircle.getParent()).findViewById(R.id.tv);

            try {
                Calendar cal = date2Cal(dateStr, datePattern);
                if (cal.get(Calendar.DAY_OF_MONTH) == Calendar.SUNDAY || holidays.contains((cal2date(cal, datePattern)))){
                    oldTv.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                } else {
                    oldTv.setTextColor(Color.WHITE);
                }
            } catch (ParseException e) {
                logStackTrace(e);
            }
        }

        View newCircle = view.findViewById(R.id.iv);
        newCircle.setTag(TAG_VISIBLE);
        newCircle.setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.tv)).setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        String dateStr = Integer.toString((Integer) view.getTag());
        Log.d(TAG, "onClick: dateStr" + dateStr);
        try {
            Calendar cal = date2Cal(dateStr, datePattern);
            callback.postOnClick(cal);
        } catch (ParseException e) {
            logStackTrace(e);
        }
    }

    public interface AdapterCallback {
        void postOnClick(Calendar cal);
    }

    private List<String> makeWofList(int startOfWeek){
        List<String> listDayOfWeek = new LinkedList<>(Arrays.asList(context.getResources().getStringArray(R.array.dof_en)));
        for (int i = 0; i < startOfWeek; i++) {
            String head = listDayOfWeek.remove(0);
            listDayOfWeek.add(head);
        }
        return listDayOfWeek;
    }
}

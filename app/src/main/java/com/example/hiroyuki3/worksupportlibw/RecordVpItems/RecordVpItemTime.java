/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.RecordVpItems;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEvent;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEventDataSet;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEventRange;
import com.example.hiroyuki3.worksupportlibw.Adapters.TimeEventRVAdapter;
import com.example.hiroyuki3.worksupportlibw.Adapters.TimeEventRangeRVAdapter;
import com.example.hiroyuki3.worksupportlibw.R;
import com.example.hiroyuki3.worksupportlibw.R2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.INVISIBLE;
import static com.cks.hiroyuki2.worksupprotlib.Util.getCopyOfCal;
import static com.cks.hiroyuki2.worksupprotlib.Util.getTimeEveDataSetFromRecordData;
import static com.cks.hiroyuki2.worksupprotlib.Util.initRecycler;
import static com.cks.hiroyuki2.worksupprotlib.UtilSpec.colorId;
import static com.example.hiroyuki3.worksupportlibw.Adapters.RecordVPAdapter.DATA_NUM;
import static com.example.hiroyuki3.worksupportlibw.Adapters.TimeEventRVAdapter.TIME_EVENT;
import static com.example.hiroyuki3.worksupportlibw.Adapters.TimeEventRangeRVAdapter.POS_IN_LIST;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_BLANK_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_EDIT_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_RECORD_FRAG;

/**
 * RecordVpItem兄弟！Timelineおじさん！ RecordFragment, EditRecordFragmentからnew される
 */
public class RecordVpItemTime extends RecordVpItem {

    private static final String TAG = "MANUAL_TAG: " + RecordVpItemTime.class.getSimpleName();
    private TimeEventRVAdapter adapter;
    private List<Pair<TimeEventRangeParams, TimeEventRangeRVAdapter>> rangePairList = new ArrayList<>();
    private TimeEventDataSet dataSet;
    public static final String DIALOG_TAG_RANGE_COLOR = "DIALOG_TAG_RANGE_COLOR";
    public static final int CALLBACK_RANGE_COLOR = 2048;
    public static String TIME_EVE_RANGE = "TIME_EVE_RANGE";
    private View view;
    private IRecordVpItemTime listener;
    @BindView(R2.id.time_event_rv) RecyclerView timeEventRv;
    @BindView(R2.id.rv_container) LinearLayout container;
    @BindView(R2.id.add_range) ImageView addRange;
    @BindView(R2.id.info_btn) ImageButton infoBtn;

    private int code;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {CODE_RECORD_FRAG, CODE_EDIT_FRAG, CODE_BLANK_FRAG})
    private @interface fragCode {}

    public RecordVpItemTime(@NonNull RecordData data, int dataNum, Calendar cal, @NonNull Fragment fragment, @Nullable IRecordVpItemTime listener, @fragCode int code) {
        super(data, dataNum, cal, fragment);
        this.code = code;
        this.listener = listener;
    }

    public RecordVpItemTime(@NonNull RecordData data, int dataNum, @NonNull Fragment fragment, @Nullable IRecordVpItemTime listener, @fragCode int code){
        this(data, dataNum, Calendar.getInstance(), fragment, listener, code);
    }

    public interface IRecordVpItemTime{
        public void onClickColorFl(Bundle bundle);
        public void onClickAddTimeEveBtn(Bundle bundle);
    }

    /**
     * posInListは、アイテムが削除されるたびに値が変更されることに注意してください。
     */
    class TimeEventRangeParams {
        @BindView(R2.id.start_circle) ImageView startCircle;
        @BindView(R2.id.end_circle) ImageView endCircle;
        @BindView(R2.id.stroke) View stroke;
        @BindView(R2.id.rv) RecyclerView rv;
        @BindView(R2.id.color_fl) FrameLayout colorFl;
        int posInList;

        TimeEventRangeParams(int posInList){
            this.posInList = posInList;
        }

        void setColor(int colorRes){
            stroke.setBackgroundResource(colorRes);
            startCircle.setColorFilter(ContextCompat.getColor(getFragment().getContext(), colorRes));
            endCircle.setColorFilter(ContextCompat.getColor(getFragment().getContext(), colorRes));
        }

        @OnClick(R2.id.remove)
        void onClickRemove(View view){
            removeRangeItem(posInList);
        }

        @OnClick(R2.id.color_fl)
        void onClickColorFl(){
            if (listener == null) return;

            Bundle bundle = new Bundle();
            bundle.putInt(DATA_NUM, getDataNum());
            bundle.putInt(POS_IN_LIST, posInList);
            bundle.putSerializable(TIME_EVE_RANGE, dataSet.getRangeList().get(posInList));
            listener.onClickColorFl(bundle);

//            if (getFragment() instanceof RecordFragment)
//                return;
//
//            kickCircleAndInputDialog(DIALOG_TAG_RANGE_COLOR, CALLBACK_RANGE_COLOR, bundle, getFragment());
        }
    }

    /**
     * {@link RecordData#data}のエントリはただ一つ、かつkey="0"である
     */
    @Override
    @Nullable
    public View buildView() {
        view = getFragment().getLayoutInflater().inflate(R.layout.record_vp_item_timeline2, null);
        ButterKnife.bind(this, view);

        infoBtn.setVisibility(INVISIBLE);//とりあえず今は使っていないから

        dataSet = getTimeEveDataSetFromRecordData(getData());
        if (dataSet == null)
            return null;

        timeEventRv.setLayoutManager(new LinearLayoutManager(getFragment().getContext()));
        timeEventRv.setNestedScrollingEnabled(false);
        adapter = new TimeEventRVAdapter(dataSet.getEventList(), getFragment(), getCal(), getDataNum());
        timeEventRv.setAdapter(adapter);

        for (int i=0; i<dataSet.getRangeList().size(); i++) {
            TimeEventRange range = dataSet.getRangeList().get(i);
            addRangeItem(range, i);
        }
        return view;
    }

    @OnClick(R2.id.add_time_eve)
    void onClickAddTimeEveBtn() {
        Bundle bundle = new Bundle();
        bundle.putInt(DATA_NUM, getDataNum());
        TimeEvent timeEvent = new TimeEvent("", 0, getCopyOfCal(getCal()), 0);
        bundle.putSerializable(TIME_EVENT, timeEvent);
//        kickTimePickerDialog(DIALOG_TAG_ITEM_ADD, CALLBACK_ITEM_ADD, bundle, getFragment());

        if (listener != null)
            listener.onClickAddTimeEveBtn(bundle);
    }

    @OnClick(R2.id.add_range)
    void onClickAddRangeBtn() {
        TimeEvent start = new TimeEvent("起床", 0, getCopyOfCal(getCal()), 0);
        TimeEvent end = new TimeEvent("就寝", 0, getCopyOfCal(getCal()), 0);
        TimeEventRange range = new TimeEventRange(start, end);

        addRangeItem(range, rangePairList.size());
        addRangeToList(range);
    }

    public void updateTime(@IntRange(from = 0) int dataNum, TimeEvent timeEvent) {
        adapter.update();
    }

    public void addItem(TimeEvent timeEvent) {
        adapter.addItem(timeEvent);
    }

    public TimeEventDataSet getDataSet() {
        return dataSet;
    }

    private void addRangeItem(@NonNull TimeEventRange range, int i){
        View v = getFragment().getLayoutInflater().inflate(R.layout.timeeve_range_container, container, false);
        container.addView(v, container.getChildCount() - 1);

        TimeEventRangeParams params = new TimeEventRangeParams(i);
        ButterKnife.bind(params, v);
        if (code == CODE_RECORD_FRAG || code == CODE_BLANK_FRAG){
            params.colorFl.setBackground(null);
            params.colorFl.setForeground(null);
        }
//        if (getFragment() instanceof RecordFragment) {
//            params.colorFl.setBackground(null);
//            params.colorFl.setForeground(null);
//        }
        int colorIdC = colorId.get(range.getColorNum());
        params.setColor(colorIdC);
        TimeEventRangeRVAdapter rangeAdapter = new TimeEventRangeRVAdapter(getFragment(), range, getDataNum(), i);
        initRecycler(getFragment().getContext(), params.rv, rangeAdapter);
        rangePairList.add(new Pair<>(params, rangeAdapter));
    }

    void removeRangeItem(int posInList){
        rangePairList.remove(posInList);
        container.removeViewAt(posInList+3);// recycler、"+"ボタン、仕切り線の分を追加
        for (int i = 0; i < rangePairList.size(); i++) {
            rangePairList.get(i).first.posInList = i;
            rangePairList.get(i).second.setPosInList(i);
        }

        dataSet.getRangeList().remove(posInList);
    }

    public void updateRangeTime(TimeEvent timeEvent, int pos, int posInList){
        rangePairList.get(posInList).second.updateTime(timeEvent, pos);
    }

    public void updateRangeValue(TimeEvent timeEvent, int pos, int posInList){
        rangePairList.get(posInList).second.updateValue();
    }

    public void updateRangeColor(TimeEventRange range, int posInList){
        rangePairList.get(posInList).first.setColor(colorId.get(range.getColorNum()));
    }

    private void addRangeToList(@NonNull TimeEventRange range){
        dataSet.getRangeList().add(range);
    }

    public View getView(){
        return view;
    }
}

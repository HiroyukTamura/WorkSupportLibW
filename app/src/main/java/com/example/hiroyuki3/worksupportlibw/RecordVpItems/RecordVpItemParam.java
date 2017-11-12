/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.RecordVpItems;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;
import com.example.hiroyuki3.worksupportlibw.Adapters.RecordParamsRVAdapter;
import com.example.hiroyuki3.worksupportlibw.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cks.hiroyuki2.worksupprotlib.Util.bundle2DataParams;
import static com.cks.hiroyuki2.worksupprotlib.Util.cal2date;
import static com.cks.hiroyuki2.worksupprotlib.Util.data2BundleParams;
import static com.cks.hiroyuki2.worksupprotlib.Util.datePattern;
import static com.cks.hiroyuki2.worksupprotlib.Util.setNullableText;
import static com.cks.hiroyuki2.worksupprotlib.Util.setRecycler;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_BLANK_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_EDIT_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_RECORD_FRAG;

/**
 * アセット兄弟！Paramsおじさん！
 */

public class RecordVpItemParam extends RecordVpItem implements RecordParamsRVAdapter.IRecordParamsRVAdapter {
    private static final String TAG = "MANUAL_TAG: " + RecordVpItemParam.class.getSimpleName();
    private OnClickParamsNameListener listener;

    @BindView(R2.id.tag_pool_name) TextView name;
    @BindView(R2.id.add_btn) ImageView addBtn;
    @BindView(R2.id.recycler) RecyclerView recycler;
    @BindColor(R2.color.blue_gray_light) int draggingColor;
    private RecordParamsRVAdapter adapter;

    private int code;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {CODE_BLANK_FRAG, CODE_EDIT_FRAG, CODE_RECORD_FRAG})
    private  @interface fragCode {}

    public interface OnClickParamsNameListener{// TODO: 2017/11/08 これ名前かえるべき
        public void onClickParamsName(int dataNum);
        public void onClickParamsAddBtn(int dataNum);
        public void syncFirebaseAndMap(int dataNum, String date, RecordData data);
        public void onClickKey(Bundle bundle, int pos, int dataNum);
        public void onClickMax(Bundle bundle, int pos, int dataNum);
    }

    public RecordVpItemParam(RecordData data, int dataNum, @Nullable Calendar cal, Fragment fragment, @Nullable OnClickParamsNameListener listener, @fragCode int code){
        super(data, dataNum, cal, fragment);

        this.code = code;
        this.listener = listener;
    }

    @Override @Nullable
    public View buildView() {

        List<Bundle> listBundle = data2BundleParams(getData());
        if (listBundle.isEmpty())
            return null;

        View view = getFragment().getLayoutInflater().inflate(R.layout.record_vp_item_params, null);
        ButterKnife.bind(this, view);
        setNullableText(name, getData().dataName);
        adapter = new RecordParamsRVAdapter(listBundle, getDataNum(), getData().dataName, getFragment(), this, this, code);
        setRecycler(getFragment().getContext(), view, adapter, R.id.recycler);

//        if (getFragment() instanceof RecordFragment)
//            return view;
        if (code == CODE_RECORD_FRAG)
            return view;

        addBtn.setVisibility(View.VISIBLE);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                adapter.swap(fromPos, toPos);
                adapter.updateData();
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                    viewHolder.itemView.setBackgroundColor(draggingColor);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
            }
        });

        itemTouchHelper.attachToRecyclerView(recycler);
        recycler.addItemDecoration(itemTouchHelper);

        return view;
    }

    public void syncFirebaseAndMap(List<Bundle> bundles){
        if (listener == null) return;

        RecordData data = bundle2DataParams(bundles, getData().dataName, getCal());
        super.setData(data);
        String date = cal2date(getCal(), datePattern);
//        int dateInt = Integer.parseInt(date);
        listener.syncFirebaseAndMap(getDataNum(), date, data);
//        List<RecordData> list = fragment.adapter.retrieveList(dateInt);
//        if (list == null) return;
//        list.set(getDataNum(), data);
//        fragment.adapter.syncDataMapAndFireBase(list, date);
    }

    @OnClick(R2.id.tag_pool_name)
    void onClickName(){
        if (listener != null)
            listener.onClickParamsName(getDataNum());
    }

    @OnClick(R2.id.add_btn)
    void onClickAddBtn(){
        if (listener != null)
            listener.onClickParamsAddBtn(getDataNum());
    }

    public void updateName(String title){
        setNullableText(name, title);
    }

    public void updateItemValue(int pos, String[] newArr){
        adapter.updateItemValue(pos, newArr);
        adapter.notifyDataSetChanged();
    }

    public void addItem(@NonNull Bundle bundle){
        adapter.add(bundle);
        adapter.updateData();
    }

    @Override
    public void onClickKey(Bundle bundle, int pos, int dataNum) {
        if (listener != null)
            listener.onClickKey(bundle, pos, dataNum);
    }

    @Override
    public void onClickMax(Bundle bundle, int pos, int dataNum) {
        if (listener != null)
            listener.onClickMax(bundle, pos, dataNum);
    }
}

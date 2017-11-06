/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.RecordVpItems;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupprotlib.UtilSpec;
import com.example.hiroyuki3.worksupportlibw.R;
import com.example.hiroyuki3.worksupportlibw.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.VISIBLE;
import static com.cks.hiroyuki2.worksupprotlib.Util.delimiter;

/**
 * EditTemplateFragment専属。
 */

class TempItemTag {
    private static final String TAG = "MANUAL_TAG: " + TempItemTag.class.getSimpleName();
    @BindView(R2.id.tv) TextView tv;
    @BindView(R2.id.card_container) CardView cv;
    @BindView(R2.id.remove) ImageView removeBtn;
    private View view;
    private int tagNum;
    private String value;
    private int dataNum;
    private Fragment frag;

    TempItemTag(int tagNum, String value, int dataNum, Fragment frag){
        this.tagNum = tagNum;
        this.value = value;
        this.dataNum = dataNum;
        this.frag = frag;
    }
    
    interface ITempItemTag{
        void onClickRemoveTagBtn(int dataNum, int tagNum);
        // TODO: 2017/11/06 次作業ここから 
    }

    View buildView(){
        view = frag.getLayoutInflater().inflate(R.layout.record_vp_item_tagitem, null);
        view.setTag(tagNum);
        ButterKnife.bind(this, view);
        removeBtn.setVisibility(VISIBLE);
        setTextAndColor();
        return view;
    }

    View getView() {
        return view;
    }

    @OnClick(R2.id.remove)
    void onClickRemoveTagBtn(){
        frag.onClickRemoveTagBtn(dataNum, tagNum);
    }

    @OnClick(R2.id.tv)
    void onClickTv(){
        frag.onClickTag(tagNum, dataNum, value);
    }

    void updateDataNum(int dataNum){
        this.dataNum = dataNum;
        view.setTag(dataNum);
    }

    void updateValue(@NonNull String value){
        this.value = value;
        setTextAndColor();
    }

    private void setTextAndColor(){
        final String[] strings = value.split(delimiter);
        int color = UtilSpec.colorId.get(Integer.parseInt(strings[1]));
        tv.setText(strings[0]);
        cv.setCardBackgroundColor(ContextCompat.getColor(frag.getContext(), color));
    }
}

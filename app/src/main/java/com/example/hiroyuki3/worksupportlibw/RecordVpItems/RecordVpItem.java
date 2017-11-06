/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.RecordVpItems;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;

import java.util.Calendar;

/**
 * RecordVpItemsの元締め。このパッケージのクラスは全てこのクラスの子です。
 */

public abstract class RecordVpItem {

    private RecordData data;
    private int dataNum;
    private Fragment fragment;
    private Calendar cal;

    public RecordVpItem(@NonNull RecordData data, int dataNum, @Nullable Calendar cal, @NonNull Fragment fragment){
        this.data = data;
        this.dataNum = dataNum;
        this.fragment = fragment;
        this.cal = cal;
    }

    public int getDataNum() {
        return dataNum;
    }

    public RecordData getData() {
        return data;
    }

    @Nullable
    public Calendar getCal() {
        return cal;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setData(RecordData data) {
        this.data = data;
    }

    public abstract View buildView();
}

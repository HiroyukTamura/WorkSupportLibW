/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.RecordVpItems;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;
import com.example.hiroyuki3.worksupportlibw.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static com.cks.hiroyuki2.worksupprotlib.Util.delimiter;
import static com.cks.hiroyuki2.worksupprotlib.Util.onError;
import static com.cks.hiroyuki2.worksupprotlib.Util.setNullableText;
/**
 * Created by hiroyuki2 on 2017/09/17.
 */

public class RecordVpItemTagPool extends RecordVpItem {
    private static final String TAG = "MANUAL_TAG: " + RecordVpItemTagPool.class.getSimpleName();
    private List<RecordVpItemTag> tagList = new ArrayList<>();
    @BindView(R2.id.tag_pool_name) TextView name;
    @BindView(R2.id.tag_box) FlowLayout ll;

    private onClickCardListener listener;

    public interface onClickCardListener{
        void onClickTagPoolContent(Calendar cal, int dataNum);
    }

    public RecordVpItemTagPool(RecordData data, int dataNum, Calendar cal, Fragment fragment, onClickCardListener listener) {
        super(data, dataNum, cal, fragment);
        this.listener = listener;
    }

    @Override
    public View buildView() {
        RelativeLayout v = (RelativeLayout) getFragment().getLayoutInflater().inflate(R.layout.record_vp_item_tags, null);
        ButterKnife.bind(this, v);

        setNullableText(name, getData().dataName);

        if (getData().data != null && !getData().data.isEmpty())
            for (String key : getData().data.keySet()) {
                final String value = (String) getData().data.get(key);

                if (value != null && !value.isEmpty()){
                    String[] strings = value.split(delimiter);
                    RecordVpItemTag tag = new RecordVpItemTag(getFragment().getContext());
                    tagList.add(tag);
                    View view = tag.buildView(strings[0], strings[1]);
                    if (!Boolean.parseBoolean(strings[2]))
                        view.setVisibility(GONE);

                    ll.addView(view);
                }
            }

        return v;
    }

    public void updateTag(){
        if (getData().data != null && !getData().data.isEmpty()){
            for (int i = 0; i < getData().data.size(); i++) {
                final String value = (String) getData().data.get(Integer.toString(i));
                if (value != null && !value.isEmpty()){
                    RecordVpItemTag tag = tagList.get(i);
                    String[] strings = value.split(delimiter);
                    tag.setTagVisible(Boolean.parseBoolean(strings[2]));
                }
            }
        } else {
            onError(getFragment(), TAG + "! getData().data != null && !getData().data.isEmpty()である", R.string.error);
        }
    }

    @OnClick(R2.id.tag_box_card)
    void onClickTagPoolContent(){
        listener.onClickTagPoolContent(getCal(), getDataNum());
    }
}

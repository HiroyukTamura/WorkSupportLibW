package com.example.hiroyuki3.worksupportlibw;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.hiroyuki3.worksupportlibw.RecordVpItems.RecordVpItemTime;

import butterknife.BindView;
import butterknife.OnClick;

import static com.example.hiroyuki3.worksupportlibw.Adapters.RecordVPAdapter.DATA_NUM;
import static com.example.hiroyuki3.worksupportlibw.Adapters.TimeEventRangeRVAdapter.POS_IN_LIST;
import static com.example.hiroyuki3.worksupportlibw.RecordVpItems.RecordVpItemTime.TIME_EVE_RANGE;

/**
 * posInListは、アイテムが削除されるたびに値が変更されることに注意してください。
 */
public class TimeEventRangeParams {
        @BindView(R2.id.start_circle) ImageView startCircle;
        @BindView(R2.id.end_circle) ImageView endCircle;
        @BindView(R2.id.stroke) View stroke;
        @BindView(R2.id.rv) RecyclerView rv;
        @BindView(R2.id.color_fl) FrameLayout colorFl;
        private RecordVpItemTime vpItemTime;
        private int posInList;

        public TimeEventRangeParams(RecordVpItemTime vpItemTime, int posInList){
            this.posInList = posInList;
        }

        public void setColor(int colorRes){
            stroke.setBackgroundResource(colorRes);
            startCircle.setColorFilter(ContextCompat.getColor(vpItemTime.getFragment().getContext(), colorRes));
            endCircle.setColorFilter(ContextCompat.getColor(vpItemTime.getFragment().getContext(), colorRes));
        }

        @OnClick(R2.id.remove)
        void onClickRemove(View view){
            vpItemTime.removeRangeItem(posInList);
        }

        @OnClick(R2.id.color_fl)
        void onClickColorFl(){
            if (vpItemTime.getListener() == null) return;

            Bundle bundle = new Bundle();
            bundle.putInt(DATA_NUM, vpItemTime.getDataNum());
            bundle.putInt(POS_IN_LIST, posInList);
            bundle.putSerializable(TIME_EVE_RANGE, vpItemTime.getDataSet().getRangeList().get(posInList));
            vpItemTime.getListener().onClickColorFl(bundle);

//            if (getFragment() instanceof RecordFragment)
//                return;
//
//            kickCircleAndInputDialog(DIALOG_TAG_RANGE_COLOR, CALLBACK_RANGE_COLOR, bundle, getFragment());
        }

    public void setPosInList(int posInList) {
        this.posInList = posInList;
    }

    public FrameLayout getColorFl() {
        return colorFl;
    }

    public RecyclerView getRv() {
        return rv;
    }
}

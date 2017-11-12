/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;
import com.example.hiroyuki3.worksupportlibw.R;
import com.example.hiroyuki3.worksupportlibw.RecordVpItems.RecordVpItemParam;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.VISIBLE;
import static com.cks.hiroyuki2.worksupprotlib.TemplateEditor.writeTemplate;
import static com.cks.hiroyuki2.worksupprotlib.Util.INDEX;
import static com.cks.hiroyuki2.worksupprotlib.Util.PARAMS_VALUES;
import static com.cks.hiroyuki2.worksupprotlib.Util.TEMPLATE_PARAMS_SLIDER_MAX;
import static com.cks.hiroyuki2.worksupprotlib.Util.bundle2Data;
import static com.cks.hiroyuki2.worksupprotlib.Util.bundle2DataParams;
import static com.cks.hiroyuki2.worksupprotlib.Util.onError;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_BLANK_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_EDIT_FRAG;
import static com.example.hiroyuki3.worksupportlibw.AdditionalUtil.CODE_RECORD_FRAG;

/**
 * RecordVpItemParam所属！うーん、入り組んでる！
 */

public class RecordParamsRVAdapter extends RecyclerView.Adapter<RecordParamsRVAdapter.ViewHolder> implements CompoundButton.OnCheckedChangeListener, DiscreteSeekBar.OnProgressChangeListener {

    private static final String TAG = "MANUAL_TAG: " + RecordParamsRVAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private List<Bundle> list;
    private int dataNum;
    private String dataName;
    private Fragment fragment;
//    private int indexMax;
    private RecordVpItemParam param;
    private IRecordParamsRVAdapter listener;
    private int code;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {CODE_EDIT_FRAG, CODE_RECORD_FRAG, CODE_BLANK_FRAG})
    private @interface fragCode {}

    public RecordParamsRVAdapter(@NonNull List<Bundle> list, int dataNum, @Nullable String dataName, @NonNull Fragment fragment, @Nullable RecordVpItemParam param, @Nullable IRecordParamsRVAdapter listener, @fragCode int code){
        Log.d(TAG, "RecordParamsRVAdapter: constructor fire");
        this.list = list;
        this.dataNum = dataNum;
        this.dataName = dataName;
        this.fragment = fragment;
        this.param = param;
        this.code = code;
//        indexMax = list.size()-1;
        inflater = (LayoutInflater)fragment.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);/*非同期でないのでwarning無視*/
        this.listener = listener;
    }

//    public RecordParamsRVAdapter(@NonNull List<Bundle> list, int dataNum, @Nullable String dataName, @NonNull Fragment fragment, @fragCode int code){//todo 後でこれなくすこと
//        this(list, dataNum, dataName, fragment, null, code);
//    }

    public interface IRecordParamsRVAdapter{
        public void onClickKey(Bundle bundle);
        public void onClickMax(Bundle bundle);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.key) TextView key;
        @BindView(R2.id.checkbox) CheckBox checkBox;
        @BindView(R2.id.seek_bar) DiscreteSeekBar seekBar;
        @BindView(R2.id.remove) ImageView remove;
        @BindView(R2.id.max) ImageView max;
        @BindView(R2.id.container) LinearLayout container;
        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.record_params_rv_item, parent, false);
        ButterKnife.bind(this, v);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.container.setTag(position);

        final Bundle bundle = list.get(position);
        final String[] values = bundle.getStringArray(PARAMS_VALUES);
        if (values == null) return;
//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (v.getId()){
//                    case R.id.key:{
//                        DialogKicker.makeBundleInOnClick(bundle, Util.TEMPLATE_PARAMS_ITEM, dataNum);
//                        DialogKicker.kickDialogInOnClick(Util.TEMPLATE_PARAMS_ITEM, Util.CALLBACK_TEMPLATE_PARAMS_ITEM, bundle, fragment);
//                        break;}
//                    case R.id.max:{
//                        DialogKicker.makeBundleInOnClick(bundle, Util.TEMPLATE_PARAMS_SLIDER_MAX, dataNum);
//                        DialogKicker.kickDialogInOnClick(Util.TEMPLATE_PARAMS_SLIDER_MAX, Util.CALLBACK_TEMPLATE_PARAMS_SLIDER_MAX, bundle, fragment);
//                        break;}
//                    case R.id.remove:{
//                        list.remove(holder.getAdapterPosition());
//                        updateData();
//                        break;}
//                }
//            }
//        };
        holder.key.setText(values[1]);
//        if (fragment instanceof EditTemplateFragment)
//            holder.key.setOnClickListener(listener);
        switch (values[0]){
            case "0":{
                holder.checkBox.setChecked(Boolean.parseBoolean(values[2]));
                holder.checkBox.setVisibility(VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(this);
//                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        values[2] = Boolean.toString(isChecked);
//                        callBack(values, holder.getAdapterPosition());
//                    }
//                });
                break;}

            case "1":{
                holder.seekBar.setMax(Integer.parseInt(values[3]));
                holder.seekBar.setProgress(Integer.parseInt(values[2]));
                holder.seekBar.setVisibility(VISIBLE);
                holder.seekBar.setOnProgressChangeListener(this);
//                holder.seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
//                    @Override
//                    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {}
//
//                    @Override
//                    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}
//
//                    @Override
//                    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
//                        values[2] = Integer.toString(seekBar.getProgress());
//                        callBack(values, holder.getAdapterPosition());
//                    }
//                });

                if (code != CODE_EDIT_FRAG)
//                if (!(fragment instanceof EditTemplateFragment))
                    break;

                holder.max.setVisibility(VISIBLE);
//                holder.max.setOnClickListener(listener);
                break;}
        }

//        if (!(fragment instanceof EditTemplateFragment))
        if (code != CODE_EDIT_FRAG) return;

        holder.remove.setVisibility(VISIBLE);
//        holder.remove.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ここでDialogFragmentから返されたbundleをlistにsetしないのは、返されたbundleはそもそもlistを構成しているbundleであるから。
     */
    public void updateData(){
        RecordData data = bundle2Data(list, dataName, 3, 0, 0, 0);/*bundle2DataParamsでなくていいのか？*/
        boolean success = writeTemplate(dataNum, data, fragment.getContext());
        if (success)
            notifyDataSetChanged();
        else
            Toast.makeText(fragment.getContext(), R.string.template_failure, Toast.LENGTH_LONG).show();
    }

    public void swap(int fromPos, int toPos){
        Bundle bundle = list.remove(fromPos);
        list.add(toPos, bundle);
    }

    public void add(@NonNull Bundle bundle){
//        indexMax++;
//        bundle.putInt(INDEX, indexMax);
        list.add(bundle);
    }

    //region seekBar/CheckBox OnChange系列
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int pos = (int) ((ViewGroup)compoundButton.getParent()).getTag();
        Bundle bundle = list.get(pos);
        String[] values = bundle.getStringArray(PARAMS_VALUES);
        if (values == null){
            onError(fragment, TAG + "onCheckedChanged", null);
            return;
        }

        values[2] = Boolean.toString(isChecked);
        callBack(values, pos);
    }

    /////////////////////////////seekBarOnChange系列ここから////////////////////////*/
    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
        int pos = (int) ((ViewGroup)seekBar.getParent()).getTag();
        Bundle bundle = list.get(pos);
        String[] values = bundle.getStringArray(PARAMS_VALUES);
        if (values == null){
            onError(fragment, TAG + "onCheckedChanged", null);
            return;
        }

        values[2] = Integer.toString(seekBar.getProgress());
        callBack(values, pos);
    }
    /////////////////////////////seekBarOnChange系列ここまで////////////////////////*/

    private void callBack(String[] values, int pos){
        Bundle bundle = list.get(pos);
        bundle.putStringArray(PARAMS_VALUES, values);
        if (code == CODE_EDIT_FRAG){
            RecordData data = bundle2DataParams(list, dataName, 0, 0, 0);
            boolean success = writeTemplate(dataNum, data, fragment.getContext());/*非同期でないからOK*/
            if (!success)
                onError(fragment.getContext(), "!success", R.string.template_failure);
        } else if (code == CODE_RECORD_FRAG){
            param.syncFirebaseAndMap(list);
        }
    }
    //endregion

    //region onClick系列
    @OnClick({R2.id.key, R2.id.max, R2.id.remove})
    void onClickBtn(View view){
        if (code != CODE_RECORD_FRAG)
            return;

        int pos = (int) ((ViewGroup)view.getParent()).getTag();

        int id = view.getId();
        if (id == R.id.key){
            Bundle bundle = list.get(pos);
            bundle.putInt(INDEX, pos);
            if (listener != null)
                listener.onClickKey(bundle);
        } else if (id == R.id.max) {
//            onClickMax(pos);
            Bundle bundle = list.get(pos);
            bundle.putInt(TEMPLATE_PARAMS_SLIDER_MAX, pos);
            if (listener != null)
                listener.onClickMax(bundle);
        } else if (id == R.id.remove) {
            onClickRemove(pos);
        }
    }

//    private void onClickKey(int pos){
//        Bundle bundle = list.get(pos);
//        bundle.putInt(INDEX, pos);
//        makeBundleInOnClick(bundle, TEMPLATE_PARAMS_ITEM, dataNum);
//        kickInputDialog(bundle, TEMPLATE_PARAMS_ITEM, CALLBACK_TEMPLATE_PARAMS_ITEM, fragment);
//    }

//    private void onClickMax(int pos){
//        Bundle bundle = list.get(pos);
//        makeBundleInOnClick(bundle, TEMPLATE_PARAMS_SLIDER_MAX, dataNum);
//        bundle.putInt(TEMPLATE_PARAMS_SLIDER_MAX, pos);
//        kickDialogInOnClick(TEMPLATE_PARAMS_SLIDER_MAX, CALLBACK_TEMPLATE_PARAMS_SLIDER_MAX, bundle, fragment);
//    }

    private void onClickRemove(int pos){
        list.remove(pos);
        updateData();
    }
    //endregion

    public void updateItemValue(int pos, String[] newArr){
        list.get(pos).putStringArray(PARAMS_VALUES, newArr);
    }
}

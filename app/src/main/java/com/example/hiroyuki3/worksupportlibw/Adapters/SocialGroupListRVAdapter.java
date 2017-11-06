/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.Entity.GroupInUserDataNode;
import com.example.hiroyuki3.worksupportlibw.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * SocialFragment所属。GroupListを表示するRecyclerViewのAdapter
 */

public class SocialGroupListRVAdapter extends RecyclerView.Adapter {

    private List<GroupInUserDataNode> list;
    private Fragment fragment;
    private Drawable defaultDrw;
    private ISocialGroupListRVAdapter listener;
    public static final String GROUP = "GROUP";
    public  static final String TAG_GROUP_NON_ADDED = "TAG_GROUP_NON_ADDED";
    public static final int CALLBACK_GROUP_NON_ADDED = 8674;

    public SocialGroupListRVAdapter(@NonNull List<GroupInUserDataNode> list, @NonNull Fragment fragment){
        super();
        this.list =list;
        this.fragment = fragment;
        defaultDrw = new ColorDrawable(ContextCompat.getColor(fragment.getContext(), R.color.colorAccent));
        if (fragment instanceof ISocialGroupListRVAdapter)
            listener = (ISocialGroupListRVAdapter) fragment;
    }

    interface ISocialGroupListRVAdapter{
        void showBoard(GroupInUserDataNode node);
        void showDialog(GroupInUserDataNode node);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R2.id.name) TextView name;
        @BindView(R2.id.icon) CircleImageView icon;
        @BindView(R2.id.container) LinearLayout container;
        @BindView(R2.id.icon_inner) ImageView iconInner;
        @BindColor(R2.color.green_light) int greenLight;
        @BindColor(android.R.color.white) int white;
        ViewHolder(View v){
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = fragment.getLayoutInflater().inflate(R.layout.social_list_rv_item, parent, false);
        ButterKnife.bind(this, v);
        return new ViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder) holder).container.setTag(position);
        GroupInUserDataNode group = list.get(position);
        ((ViewHolder) holder).name.setText(group.name);

        int color;
        if (group.added)
            color =  ((ViewHolder) holder).white;
        else
            color = ((ViewHolder) holder).greenLight;
        ((ViewHolder) holder).container.setBackgroundColor(color);
        ((ViewHolder) holder).icon.setImageDrawable(null);
        ((ViewHolder) holder).iconInner.setImageDrawable(null);
        Picasso.with(fragment.getContext())
                .load(group.groupKey)
                .into(((ViewHolder) holder).icon, new Callback() {
                    @Override
                    public void onError() {
                        ((ViewHolder) holder).icon.setImageDrawable(defaultDrw);
                        ((ViewHolder) holder).iconInner.setImageResource(R.drawable.ic_group_white_24dp);
                    }

                    @Override
                    public void onSuccess() {}
                });
    }

    @OnClick(R2.id.container)
    void onClickItem(LinearLayout container){
        int pos = (int)container.getTag();
        if (list.get(pos).added)
            listener.showBoard(list.get(pos));
        else {
            listener.showDialog(list.get(pos));
//            Bundle bundle = new Bundle();
//            bundle.putSerializable(GROUP, list.get(pos));
//            bundle.putString("from", TAG_GROUP_NON_ADDED);
//            kickDialogInOnClick(TAG_GROUP_NON_ADDED, CALLBACK_GROUP_NON_ADDED, bundle, fragment);
        }
    }

    public void notifyAddedToGroup(String groupKey){
        int pos = getPosWithGroupKey(groupKey);
        if (pos == Integer.MAX_VALUE)
            return;

        list.get(pos).added = true;
        notifyItemChanged(pos);
    }

    public void notifyExitGroup(String groupKey){
        int pos = getPosWithGroupKey(groupKey);
        if (pos == Integer.MAX_VALUE)
            return;

        list.remove(pos);
        notifyItemRemoved(pos);
    }

    /**
     * @return 例外としてInteger.MAX_VALUEを投げる
     */
    private int getPosWithGroupKey(String groupKey){
        int m = Integer.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).groupKey.equals(groupKey)){
                m = i;
            }
        }
        return m;
    }
}

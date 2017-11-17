/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.Entity.User;
import com.example.hiroyuki3.worksupportlibw.R;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.cks.hiroyuki2.worksupprotlib.Util.onError;
import static com.cks.hiroyuki2.worksupprotlib.Util.setImgFromStorage;
import static com.cks.hiroyuki2.worksupprotlib.Util.setNullableText;

/**
 * Groupのメンバーを表すRVAdapter. GroupSettingFragmentの舎弟。
 * itemの識別には、positionを一切使わずにUIDで識別しているところがミソ。
 */

public class GroupSettingRVAdapter extends RecyclerView.Adapter implements CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "MANUAL_TAG: " + GroupSettingRVAdapter.class.getSimpleName();
    private Fragment fragment;
    private List<User> userList;
    public static final String REMOVE_MEMBER = "REMOVE_MEMBER";
    public static final String USER = "USER";
    public static final int CALLBACK_REMOVE_MEMBER = 8731;
    private FirebaseUser userMe;
    private IGroupSettingRVAdapter listener;
    private String tagVal;
    @ColorInt private int blueGray;

    public GroupSettingRVAdapter(Fragment fragment, List<User> userList, @NonNull FirebaseUser userMe) {
        this.fragment = fragment;
        this.userList = userList;
        this.userMe = userMe;
        tagVal = fragment.getString(R.string.grp_set_invited_tag);
        blueGray = ContextCompat.getColor(fragment.getContext(), R.color.blue_gray);

        if (fragment instanceof IGroupSettingRVAdapter)
            listener = (IGroupSettingRVAdapter) fragment;
    }

    public interface IGroupSettingRVAdapter{
        public void onClickRemoveMe();
        public void onClickRemoveOthers(Bundle bundle);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.icon) CircleImageView icon;
        @BindView(R2.id.switch_widget) SwitchCompat switchWidget;
        @BindView(R2.id.remove) ImageButton remove;
        @BindView(R2.id.name) TextView name;
        @BindView(R2.id.invite_tag) View invitedTag;
        @BindView(R2.id.tv) TextView tagVal;
        @BindView(R2.id.card_container) CardView card;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = fragment.getLayoutInflater().inflate(R.layout.group_setting_rv_item, parent, false);
        ButterKnife.bind(this, v);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        User member = userList.get(position);
        String uid = member.getUserUid();
        ((ViewHolder)holder).remove.setTag(uid);
        ((ViewHolder) holder).switchWidget.setTag(uid);

        setNullableText(((ViewHolder) holder).name, member.name);
        setImgFromStorage(member, ((ViewHolder) holder).icon, R.drawable.ic_face_origin_48dp);

        if (!member.isChecked){
            ((ViewHolder) holder).invitedTag.setVisibility(VISIBLE);
            ((ViewHolder) holder).tagVal.setText(tagVal);
            ((ViewHolder) holder).card.setCardBackgroundColor(blueGray);
        } else {
            ((ViewHolder) holder).invitedTag.setVisibility(GONE);
        }
        ((ViewHolder) holder).switchWidget.setOnCheckedChangeListener(this);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @OnClick(R2.id.remove)
    public void onRemoveClick(View v) {
        String uid = (String)v.getTag();
        if (userMe.getUid().equals(uid)) {
            listener.onClickRemoveMe();
            return;
        }

        Bundle bundle = new Bundle();
        int pos = getPosFromUid(uid);
        if (pos == Integer.MAX_VALUE){
            onError(fragment, TAG+"pos == Integer.MAX_VALUE", R.string.error);
            return;
        }

        bundle.putSerializable(USER, userList.get(pos));
        bundle.putString("from", REMOVE_MEMBER);
        listener.onClickRemoveOthers(bundle);
//        kickDialogInOnClick(REMOVE_MEMBER, CALLBACK_REMOVE_MEMBER, bundle, fragment);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    /**
     * @return 例外時Integer.MAX_VALUE
     */
    public int getPosFromUid(@NonNull String uid){
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserUid().equals(uid)){
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public void removeMember(int pos){
        userList.remove(pos);
        notifyItemRemoved(pos);
    }

    public User getUser(int pos){
        return userList.get(pos);
    }
}

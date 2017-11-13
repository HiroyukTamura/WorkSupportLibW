package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hiroyuki3.worksupportlibw.R;
import com.example.hiroyuki3.worksupportlibw.R2;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * About画面まわりやる人。@see AboutFragment
 */
public class AboutVPAdapter extends PagerAdapter {

    private static final int page = 2;
    private Context context;
    private LayoutInflater inflater;
    private String title0;
    private String title1;
    private IAboutVPAdapter listener;
    private Item0 item0;

    public AboutVPAdapter(@NonNull Context context, @NonNull IAboutVPAdapter listener){
        this.context = context;
        this.listener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        title0 = context.getString(R.string.about_vp_title0);
        title1 = context.getString(R.string.about_vp_title1);
    }

    public interface IAboutVPAdapter{
        public void onClickLibItem();
        public void onClickLauncher();
        public void onClickResetData();
        public void onClickAppLicense();
        public void onClickAppTos();
    }

    public class Item0{
        @BindString(R2.string.app_name) String appName;
        @BindView(R2.id.title_ll1) TextView titleLL1;
        @OnClick(R2.id.setting_ll0)
        void onClickSetting0(){
            listener.onClickResetData();
        }
    }

    public class Item1{
        @OnClick(R2.id.ll0)
        void onClickLL0(){
            listener.onClickLibItem();
        }

        @OnClick(R2.id.ll1)
        void onClickLL1(){
            listener.onClickLauncher();
        }

        @OnClick(R2.id.about_app_ll0)
        void onClickAboutApp0(){
            listener.onClickAppLicense();
        }

        @OnClick(R2.id.about_app_ll1)
        void onClickAboutApp1(){
            listener.onClickAppTos();
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = new View(context);
        switch (position){
            case 0:
                view = inflater.inflate(R.layout.about_vp_item0, null);
                item0 = new Item0();
                ButterKnife.bind(item0, view);
                String string = item0.appName + "から退会する";
                item0.titleLL1.setText(string);
                break;
            case 1:
                view = inflater.inflate(R.layout.about_vp_item1, null);
                ButterKnife.bind(new Item1(), view);
                break;
        }
        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return page;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return title0;
            case 1:
                return title1;
        }
        return null;
    }
}

package com.example.hiroyuki3.worksupportlibw.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.hiroyuki3.worksupportlibw.R;
import com.example.hiroyuki3.worksupportlibw.R2;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.cks.hiroyuki2.worksupprotlib.Util.PREF_NAME;

/**
 * About画面まわりやる人。@see AboutFragment
 */
public class AboutVPAdapter extends PagerAdapter implements CompoundButton.OnCheckedChangeListener{

    private static final int page = 2;
    private Context context;
    private LayoutInflater inflater;
    private String title0;
    private String title1;
    private IAboutVPAdapter listener;
    private Unbinder unbinder0;
    private Unbinder unbinder1;
    private SharedPreferences pref;
    public static final String PREF_KEY_SHOW_NAV_IMG = "PREF_KEY_SHOW_NAV_IMG";

    public AboutVPAdapter(@NonNull Context context, @NonNull IAboutVPAdapter listener){
        this.context = context;
        this.listener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        title0 = context.getString(R.string.about_vp_title0);
        title1 = context.getString(R.string.about_vp_title1);
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public interface IAboutVPAdapter{
        void onClickLibItem();
        void onClickLauncher();
        void onClickResetData();
        void onClickAppLicense();
        void onClickAppTos();
        void onClickNotification();
        void onSwitchChange(boolean show);
    }


    public class Item0{
        @BindString(R2.string.app_name) String appName;
        @BindView(R2.id.title_ll1) TextView titleLL1;
        @BindView(R2.id.toggle1) SwitchCompat toggle;

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

        @OnClick(R2.id.about_app_ll3)
        void onCLickAboutApp2(){
            listener.onClickNotification();
        }
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = new View(context);
        switch (position){
            case 0:
                view = inflater.inflate(R.layout.about_vp_item0, null);
                Item0 item0 = new Item0();
                unbinder0 = ButterKnife.bind(item0, view);
                String string = item0.appName + "から退会する";
                item0.titleLL1.setText(string);
                item0.toggle.setChecked(pref.getBoolean(PREF_KEY_SHOW_NAV_IMG, true));
                item0.toggle.setEnabled(false);
                /*item0.toggle.setOnCheckedChangeListener(this);*/
                break;
            case 1:
                view = inflater.inflate(R.layout.about_vp_item1, null);
                unbinder1 = ButterKnife.bind(new Item1(), view);
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.toggle1){
            pref.edit()
                    .putBoolean(PREF_KEY_SHOW_NAV_IMG, b)
                    .apply();
            listener.onSwitchChange(b);
        }
    }

    public void unbind(){
        unbinder0.unbind();
        unbinder1.unbind();
    }
}

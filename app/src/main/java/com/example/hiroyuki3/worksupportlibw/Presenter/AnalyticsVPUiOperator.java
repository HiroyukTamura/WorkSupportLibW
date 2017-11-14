/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.example.hiroyuki3.worksupportlibw.Presenter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cks.hiroyuki2.worksupportlib.R2;
import com.cks.hiroyuki2.worksupprotlib.AnalyticsCommentObserver;
import com.cks.hiroyuki2.worksupprotlib.AnalyticsTagpoolObserver;
import com.cks.hiroyuki2.worksupprotlib.Entity.RecordData;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEvent;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEventDataSet;
import com.cks.hiroyuki2.worksupprotlib.Entity.TimeEventRange;
import com.cks.hiroyuki2.worksupprotlib.FirebaseEventHandler;
import com.cks.hiroyuki2.worksupprotlib.RecordDataUtil;
import com.cks.hiroyuki2.worksupprotlib.Util;
import com.example.hiroyuki3.worksupportlibw.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apmem.tools.layouts.FlowLayout;
import org.jetbrains.annotations.Contract;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import fr.castorflex.android.verticalviewpager.VerticalViewPager;

import static com.cks.hiroyuki2.worksupprotlib.Util.cal2date;
import static com.cks.hiroyuki2.worksupprotlib.Util.datePattern;
import static com.cks.hiroyuki2.worksupprotlib.Util.delimiter;
import static com.cks.hiroyuki2.worksupprotlib.Util.getTimeEveDataSetFromRecordData;
import static com.cks.hiroyuki2.worksupprotlib.Util.time2String;
import static com.cks.hiroyuki2.worksupprotlib.UtilSpec.colorId;
import static com.example.hiroyuki3.worksupportlibw.Adapters.AnalyticsVPAdapter.OFFSET;

/**
 * AnalyticsVPAdapterのお助けやくおじさん！みんな協力して働くんだね！
 */

public class AnalyticsVPUiOperator implements ValueEventListener, IValueFormatter, ViewTreeObserver.OnScrollChangedListener, OnChartValueSelectedListener {

    private static final String TAG = "MANUAL_TAG: " + AnalyticsVPUiOperator.class.getSimpleName();
    private static final float LINE_WIDTH = 3f;
    private static final int COLUMN_NAME_LINE_LIMIT = 15;

    @BindView(R2.id.chart) LineChart chart;
    @BindView(R2.id.scroll) HorizontalScrollView hsv;
    @BindView(R2.id.table) LinearLayout tableLL;
    @BindView(R2.id.table_time) LinearLayout tableTime;
    @BindView(R2.id.date_tv) TextView dateTv;
    @BindView(R2.id.left_screen) LinearLayout leftLL;
//    @BindView(R.id.legend_fl) FlowLayout legendFl;
    @BindDimen(R2.dimen.column_min_width) int columnMinWidth;
    @BindDimen(R2.dimen.grid_padding) int padding;
    int legendHeight;
    @BindColor(R2.color.red_anton_dark) int colorGradationMax;
    @BindColor(R2.color.blue_pint) int colorGradationMin;
    @BindColor(R2.color.blue_gray_light) int blueGrayLight;
    @BindColor(R2.color.colorAccentDark) int colorAccentDark;
    @BindColor(R2.color.colorPrimaryDark) int colorPrimaryDark;
    @BindDrawable(R2.drawable.fui_done_check_mark) Drawable check;
    @BindDrawable(R2.drawable.analytics_divider) Drawable divider;
    @BindDimen(R2.dimen.check_drw_size) int checkSize;//これでpx変換される
    @BindDimen(R2.dimen.unchek_drw_thickness) int uncheckThickSize;
    @BindDimen(R2.dimen.msv_top_margin) int mcvTopMargin;
    @BindDimen(R2.dimen.def_mp_chart_padding) int defChardPadding;
    @BindDimen(R2.dimen.wof_width) int wofWidth;
//    private LineChart chart;
    private View root;
    private ArrayList<ILineDataSet> lines = new ArrayList<> ();
    private ArrayList<Calendar> loadCal;
    private int verticalRowPad;
    private Calendar startCal;
    private LayoutInflater inflater;
    private Fragment analyticsFragment;
    private List<Pair<Integer, String>> legendListForRange = new ArrayList<>();
    private List<Pair<Integer, String>> legendListForTimeEve = new ArrayList<>();
    private IAnalyticsVPUiOperator listener;
    private List<Pair<String, Integer>> columnList = new ArrayList<>();//firstにはdataNameが、secondにはdataTypeが代入される。ただし、dataType==1の場合はcolumnTimeListに"start.name → end.name"として追加する
    private List<String> columnTimeList = new ArrayList<>();
    private String uid;
    private Unbinder unbinder;

    public AnalyticsVPUiOperator(WeakReference<View> root, Calendar startCal, Fragment analyticsFragment, @NonNull String uid, int legendHeight){
        unbinder = ButterKnife.bind(this, root.get());

        this.root = root.get();
        this.startCal = startCal;
        this.analyticsFragment = analyticsFragment;
        this.uid = uid;
        this.legendHeight = legendHeight;

        if (analyticsFragment instanceof IAnalyticsVPUiOperator)
            listener = (IAnalyticsVPUiOperator) analyticsFragment;

        initParams();
        setDate();
        initData(startCal.getTime());
        configChart();
    }

    public interface IAnalyticsVPUiOperator {
        public void onClickDownBtn();
        public void onClickUpBtn();
        public void onScrollChanged(HorizontalScrollView scrollView, int x);
    }

    private void initParams(){
        verticalRowPad = padding*2;
        hsv.getViewTreeObserver().addOnScrollChangedListener(this);
        inflater = analyticsFragment.getLayoutInflater();
//        hsv.getViewTreeObserver().addOnScrollChangedListener(this);
//        mListener = (IAnalyticsVPUiOperator)rootView.getContext();
    }

    //タイトルを設定
    private void setDate(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(startCal.getTime());
        String start = cal2date(startCal, Util.DATE_PATTERN_DOT_MD);
        cal.add(Calendar.DATE, 6);
        String end = cal2date(cal, Util.DATE_PATTERN_DOT_MD);
        String title = start + " - " + end;
        dateTv.setText(title);
    }

    private void setParamsCulumn(RecordData data){
        LinearLayout column = makeColumn();
        tableLL.addView(column);
        //まず大項目を追加
        TextView bigArticleCell = setLegendBigCellOfParams(data.dataName);
        column.addView(bigArticleCell);
        //次に小項目を追加
        LinearLayout bigColumn = makeBigArticleColumn();
        column.addView(bigColumn);

        for (String key : data.data.keySet()) {
            LinearLayout smallColumn = (LinearLayout) inflater.inflate(R.layout.analytics_columun, bigColumn, false);
            bigColumn.addView(smallColumn);
            String value = (String) data.data.get(key);
            if (value == null) continue;
            String smlArticle = value.split(delimiter)[1];

            TextView smallCell = smallColumn.findViewById(R.id.legend_cell);
            smallCell.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, legendHeight / 2));
            smallCell.setText(smlArticle);
        }
    }

    private void setNormalColumn(String columnName, LinearLayout table){
        LinearLayout column = (LinearLayout) inflater.inflate(R.layout.analytics_columun, table, false);
        table.addView(column);
        TextView legendCell = column.findViewById(R.id.legend_cell);
        if (columnName.length() > COLUMN_NAME_LINE_LIMIT)
            legendCell.setLines(2);
        legendCell.setText(columnName);
    }

    private LinearLayout makeColumn(){
        LinearLayout column = new LinearLayout(analyticsFragment.getContext());
        column.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        column.setMinimumWidth(columnMinWidth);
        column.setOrientation(LinearLayout.VERTICAL);
        return column;
    }

    private LinearLayout makeBigArticleColumn(){
        LinearLayout column = new LinearLayout(analyticsFragment.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        column.setLayoutParams(lp);
        column.setOrientation(LinearLayout.HORIZONTAL);
//        column.setPadding(0, 0, 0, mcvTopMargin);
        return column;
    }

    private TextView setLegendBigCellOfParams(String string){
        TextView tv = new TextView(analyticsFragment.getContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, legendHeight/2));
        tv.setText(string);
        tv.setPadding(padding, 0, padding, 0);
        tv.setBackgroundColor(blueGrayLight);
        tv.setTextColor(Color.BLACK);
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    private void fitChartWidth(){
        int width = root.getRootView().getWidth() - wofWidth;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        leftLL.setLayoutParams(lp);
    }

    private void initData(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("usersParam").child(uid);
        RecordDataUtil util = RecordDataUtil.getInstance();
        String[] dof = analyticsFragment.getResources().getStringArray(R.array.dof);
//        String[] axisValue = new String[7];
        loadCal = new ArrayList<>(7);
        //前週最終日と翌週初日も取得する。
        for (int i = -1; i < 8; i++) {
            final int n = i;
            String ymd = cal2date(cal, datePattern);
            int ymdInt = Integer.parseInt(ymd);

            if (util.dataMap.containsKey(ymdInt) && util.dataMap.get(ymdInt) != null && !util.dataMap.get(ymdInt).isEmpty()) {
                List<RecordData> list = util.dataMap.get(ymdInt);
                Log.d(TAG, "onCreateView: こいつデータあります！");

                if (n == -1 || n == 7) {
                    drawOffset(list, n);
                    continue;
                }
                drawData(list, n);

                loadCal.add(cal);
                if (loadCal.size() == 7)
                    showData();

                cal.add(Calendar.DATE, 1);
                continue;
            }

            DatabaseReference refer = ref.child(ymd);
            FirebaseEventHandler handler = new FirebaseEventHandler(cal) {
                @Override
                public void onOnDataChange(DataSnapshot dataSnapshot, boolean isSnapShotExist) {
                    if (analyticsFragment.getContext() != null //非同期で、且つcontextが必要な処理をするので、ここでnullチェックをします
                            &&isSnapShotExist && !list.isEmpty()){//listはnonNull、かつ、listは空でありうることに注意してください。
                        Log.w(TAG, "onOnDataChange: " + dataSnapshot.getRef().toString());
                        if (n == -1 || n == 7){
                            drawOffset(list, n);
                            return;
                        }

                        drawData(list, n);
                    }
                    loadCal.add(cal);
                    if (loadCal.size() == 7)
                        showData();
                }

                @Override
                public void onOnCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "onOnCancelled: " + databaseError.getMessage());
                    loadCal.add(cal);
                    if (loadCal.size() == 7)
                        showData();
                }
            };

            handler.initValueEventListener();
            refer.addListenerForSingleValueEvent(handler.getListener());

//            axisValue[i] = cal.get(Calendar.DATE)+ "("+dof[i]+")";

            if (n != -1 && n != 7)
                cal.add(Calendar.DATE, 1);
        }
    }

    private void drawOffset(List<RecordData> list, int n){
        RecordData data = Util.getRecordDataByType(list, 1);
        TimeEventDataSet dataSet = Util.getTimeEveDataSetFromRecordData(data);
        if (dataSet == null)
            return;

        for (TimeEventRange range: dataSet.getRangeList()) {

            switch (n){
                case -1:
                    if (range.getEnd().getOffset() != 1)
                        continue;

                    if (range.getStart().getOffset() == 1){
                        add2LineNormal(range, n, 1);
                    } else {
                        add2LineAfter24h(1, n, range);
                    }
                    break;
                case 7:
                    if (range.getStart().getOffset() != -1)
                        continue;

                    if (range.getEnd().getOffset() == -1){
                        add2LineNormal(range, n, -1);
                    } else {
                        add2LineBefore24h(-1, n, range);
                    }
                    break;
            }
        }
    }

    private void drawData(List<RecordData> list, int dataRow){

        RecordData timeLine = Util.getRecordDataByType(list, 1);
        TimeEventDataSet dataSet = Util.getTimeEveDataSetFromRecordData(timeLine);

        //まずTimeのカラムを追加
        if (dataSet != null)
            for (TimeEventRange range: dataSet.getRangeList()) {
                String name = range.getStart().getName() +" → "+ range.getEnd().getName();
                if (columnTimeList.contains(name))
                    continue;
                columnTimeList.add(name);
                setNormalColumn(name, tableTime);
            }


        //次にそれ以外のカラムを追加
        for (RecordData data : list) {
            if (data.getDataType() == 0 || data.getDataType() == 1)
                continue;

            Pair<String, Integer> pair = new Pair<>(data.getDataName(), data.getDataType());
            if (columnList.contains(pair))
                continue;

            if (data.getDataType() == 2 || data.getDataType() == 4){
                setNormalColumn(data.dataName, tableLL);
                columnList.add(pair);
            } else if (data.getDataType() == 3){
                setParamsCulumn(data);
                columnList.add(pair);
            }
        }


//        int count = 0;
        for (RecordData data: list) {
            if (data.dataType == 0)
                continue;

            if ((data.data == null || data.data.isEmpty())
                    && (data.dataType == 2 || data.dataType == 3 || data.dataType == 4)) {
//                count++;
                continue;
            }

            Pair<String, Integer> pair = new Pair<>(data.dataName, data.dataType);
            int count = columnList.indexOf(pair);
            LinearLayout column = (LinearLayout) tableLL.getChildAt(count);

            if (data.dataType == 1){
                drawLine(data, dataRow);

            } else if (data.dataType == 2) {
                FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
                addTag2Fl(data, fl);
//                count++;

            } else if (data.dataType == 3){//params
                addParams2Fl(data, column, dataRow);
//                count++;

            } else if (data.dataType == 4){
//                FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
//                TextView tv = createCommentTv(data);
//                fl.addView(tv);
                addComment2Fl(column, dataRow, data);
//                count++;
            }
        }
    }

    //region dataType == 1 系列
    private void drawLine(RecordData data, int dataRow){
        TimeEventDataSet timeEveSet = getTimeEveDataSetFromRecordData(data);
        if (timeEveSet == null)
            return;

        List<TimeEventRange> ranges = timeEveSet.getRangeList();
        for (int i = 0; i < ranges.size(); i++) {
            TimeEventRange range = ranges.get(i);
            int colorNum = range.getColorNum();
            int startOffset = range.getStart().getOffset();
            int endOffset = range.getEnd().getOffset();

            if (startOffset == endOffset){
                if ((isBeforeWeek(startOffset, dataRow)|| isAfterWeek(startOffset, dataRow))){
                    //初日の前日or最後日の翌日であれば描画しない
                    continue;
                }
                add2LineNormal(range, dataRow, startOffset);

            } else if (endOffset - startOffset == 1) {
                add2LineBefore24h(startOffset, dataRow, range);
                add2LineAfter24h(endOffset, dataRow, range);

            } else if (endOffset - startOffset == 2){
                add2LineBefore24h(startOffset, dataRow, range);
                addWithoutValueAndCircle2Line(true, 0, 0, dataRow, colorNum, createHighLightVal(range));//24時間分の線分を描画
                add2LineAfter24h(endOffset, dataRow, range);
            }

            //合計時間を計算して小数第2位で四捨五入
            float hourLong = (range.getEnd().getHourFloat() + endOffset*24) - (range.getStart().getHourFloat() + startOffset*24);
            BigDecimal bigDecimal = BigDecimal.valueOf(hourLong);
            String hourStr = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "h";

            String str = range.getStart().getName() + " → " + range.getEnd().getName();
            int num = columnTimeList.indexOf(str);
            FlowLayout fl = (FlowLayout) ((ViewGroup)tableTime.getChildAt(num)).getChildAt(dataRow+1);
            TextView tv = createNormalTv();
            tv.setText(hourStr);
            fl.addView(tv);
        }

        List<TimeEvent> eveList = timeEveSet.getEventList();
        for (TimeEvent timeEve: eveList) {
            addTimeEve2Line(timeEve.getColorNum(), timeEve.getHourFloat(), dataRow, createHighLightVal(timeEve));
        }

        setLegend(timeEveSet);
    }

    /**
     * 24時以前を描画
     */
    private void add2LineBefore24h(int startOffset, int dataRow, TimeEventRange range){
        if (!isBeforeWeek(startOffset, dataRow)){
            //●のないただの線分を描画
            addWithoutValueAndCircle2Line(true, startOffset, range.getStart().getHourFloat(), dataRow, range.getColorNum(), createHighLightVal(range));
            //次に●を片方だけ描画
            addTimeEve2Line(range.getColorNum(), range.getStart().getHourFloat(), dataRow + startOffset, createHighLightVal(range));
        }
    }

    private void add2LineAfter24h(int endOffset, int dataRow, TimeEventRange range){
        //24時以降を描画
        if (!isAfterWeek(endOffset, dataRow)){
            addWithoutValueAndCircle2Line(false, endOffset, range.getEnd().getHourFloat(), dataRow, range.getColorNum(), createHighLightVal(range));
            addTimeEve2Line(range.getColorNum(), range.getEnd().getHourFloat(), dataRow + endOffset, createHighLightVal(range));
        }
    }

    private void add2LineNormal(TimeEventRange range, int dataRow, int offset){
        List<Entry> entryList = new ArrayList<>();
        entryList.add(new Entry(range.getStart().getHourFloat(), dataRow + offset, createHighLightVal(range)));
        entryList.add(new Entry(range.getEnd().getHourFloat(), dataRow + offset, createHighLightVal(range)));
        add2Lines(entryList, range.getColorNum());
    }

    @Contract(pure = true)
    private static boolean isBeforeWeek(int startOffset, int dataRaw){
        return startOffset == -1 && dataRaw == 0;
    }

    @Contract(pure = true)
    private static boolean isAfterWeek(int endOffset, int dataRaw){
        return endOffset == 1 && dataRaw == 6;
    }

    private void addTimeEve2Line(int colorNum, float hourMin, int dataRow, String highLight){
        List<Entry> entryTimeEve = new ArrayList<>();
        entryTimeEve.add(new Entry(hourMin, dataRow, highLight));
        add2Lines(entryTimeEve, colorNum);
    }

    /**
     * 時刻も●もない線分をlinesに加える。これは、日を跨いだrangeに用いられる
     */
    private void addWithoutValueAndCircle2Line(boolean isStart, int offset, float hourMin, int dataRow, int colorNum, @NonNull String highLight){
        List<Entry> entryList = new ArrayList<>();
        int edge = isStart ? 24 : 0;
        entryList.add(new Entry(hourMin, dataRow + offset, highLight));//サークルがないのでハイライトは不要
        entryList.add(new Entry(edge, dataRow + offset, highLight));//サークルがないのでハイライトは不要

        LineDataSet dataSet = new LineDataSet(entryList, "Label");
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        int color = ContextCompat.getColor(analyticsFragment.getContext(),  colorId.get(colorNum));
        dataSet.setColor(color);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setLineWidth(LINE_WIDTH);
        lines.add(dataSet);
    }
    //endregion

    //region dataType == 2 系列
    private void addTag2Fl(RecordData data, FlowLayout fl){
        List<View> tagList = new LinkedList<>();
        for (String key: data.data.keySet()) {
            String s = (String)data.data.get(key);
            String strings[] = s.split(delimiter);
            if (strings[2].equals(Boolean.toString(false)))
                continue;
            final View view = Util.makeCircleAndTxt(analyticsFragment.getContext(), strings[0], Integer.parseInt(strings[1]));
            tagList.add(view);
        }
        new AnalyticsTagpoolObserver(fl, tagList);
    }
    //endregion

    //region dataType == 3 系列
    ///////////////////////////params系列ここから//////////////////////
    private void addParams2Fl(RecordData data, LinearLayout column, int dataRow){
        int countInner = 0;//一行目は項目名
        LinearLayout container = (LinearLayout) column.getChildAt(1);

        for (String key : data.data.keySet()) {
            String value = (String) data.data.get(key);
            if (value == null) continue;

            LinearLayout smlColumn = (LinearLayout) container.getChildAt(countInner);
            FlowLayout fl = (FlowLayout) smlColumn.getChildAt(dataRow+1);

            String[] values = value.split(delimiter);
            switch (values[0]){
                case "0":
                    FrameLayout wrapper = createCheckView(Boolean.parseBoolean(values[2]));
                    fl.addView(wrapper);
                    break;

                case "1":
                    int max = Integer.parseInt(values[3]);
                    int min = 1;
                    int val = Integer.parseInt(values[2]);
                    TextView tv = createDigitView(max, val - min, values[2]);
                    fl.addView(tv);
                    break;
            }

            countInner++;
        }
    }

    /** なぜDrawableをFrameLayoutラップしているかというと、imageViewはgravityを指定できないし、親ビューがFlowLayoutになるので、layout_gravityも効かないためです*/
    private FrameLayout createCheckView(boolean isCheck){
        ImageView iv = new ImageView(analyticsFragment.getContext());
        FrameLayout.LayoutParams lp = isCheck
                ? new FrameLayout.LayoutParams(checkSize, checkSize)
                : new FrameLayout.LayoutParams(checkSize/2, uncheckThickSize);
        lp.gravity = Gravity.CENTER;
        iv.setLayoutParams(lp);
        FrameLayout flWrapper = new FrameLayout(analyticsFragment.getContext());
        flWrapper.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        flWrapper.addView(iv);

        if (isCheck){
            Drawable checkDrw = check.mutate();
            checkDrw.setColorFilter(colorPrimaryDark, PorterDuff.Mode.SRC_IN);
            iv.setImageDrawable(checkDrw);
        } else {
            Drawable checkDrw = divider.mutate();
            checkDrw.setColorFilter(colorAccentDark, PorterDuff.Mode.SRC_IN);
            iv.setImageDrawable(checkDrw);
        }

        return flWrapper;
    }

    private TextView createDigitView(int max, int digit, @NonNull String txt){
        TextView tv = new TextView(analyticsFragment.getContext());
        tv.setTextSize(18);
        tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        tv.setText(txt);
        tv.setTextColor(Color.rgb(
                Color.red(colorGradationMin) + (Color.red(colorGradationMax) - Color.red(colorGradationMax)) * digit/max,
                Color.green(colorGradationMin) + (Color.green(colorGradationMax) - Color.green(colorGradationMax)) * digit/max,
                Color.blue(colorGradationMin) + (Color.blue(colorGradationMax) - Color.blue(colorGradationMax)) * digit/max
        ));
        return tv;
    }
    ///////////////////////////params系列ここまで//////////////////////
    //endregion

    //region dataType == 4 系列
    private void addComment2Fl(LinearLayout column, int dataRow, RecordData data){
        FlowLayout fl = (FlowLayout) column.getChildAt(dataRow+1);//一行目は項目名
        TextView tv = createCommentTv(data);
        new AnalyticsCommentObserver(fl, tv);
    }

    @NonNull
    private TextView createCommentTv(RecordData data){
        TextView tv = createNormalTv();
        if (data.data.containsKey("comment") && data.data.get("comment") != null){
            String string = (String) data.data.get("comment");
            tv.setText(string);
        }
        return tv;
    }
    //endregion

    private TextView createNormalTv(){
        TextView tv = new TextView(analyticsFragment.getContext());
        tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        return tv;
    }

    static private float hourMin2Hour(@NonNull String string){
        String[] hm = string.split(":");
        int hour = Integer.parseInt(hm[0]);
        float min = Integer.parseInt(hm[1]) /60;
        return hour+min;
    }

    private void add2Lines(List<Entry> entries, int num){
        if (entries == null || entries.isEmpty())
            return;
        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setValueFormatter(this);
        setWholeCircleColor(dataSet, colorId.get(num));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSet.setValueTextSize(12f);//これはdp指定であることに注意してください
        dataSet.setLineWidth(LINE_WIDTH);
        lines.add(dataSet);
    }

    private void setWholeCircleColor(LineDataSet dataSet, int colorId){
        int color = ContextCompat.getColor(analyticsFragment.getContext(), colorId);
        dataSet.setDrawCircleHole(false);
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
    }

    private void showData(){
        fitChartWidth();//横幅をセットする

        chart.setData(new LineData(lines));
        chart.setViewPortOffsets(defChardPadding, 0, defChardPadding, 0);
        chart.invalidate();

//        setLegendLayoutPrams();
    }

//    private void setLegendLayoutPrams(){
//        RelativeLayout.LayoutParams lp = (new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (chart.getHeight()+defChardPadding*2)/(7*2)));
//        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        legendFl.setLayoutParams(lp);
//    }

    @NonNull
    private String createHighLightVal(TimeEventRange range){
        return range.getStart().getTimeStr() +" "+ range.getStart().getName()
                +" → "
                + range.getEnd().getTimeStr() +" "+ range.getEnd().getName();
    }

    @NonNull
    private String createHighLightVal(TimeEvent event){
        return event.getTimeStr() +" "+event.getName();
    }

    private void configChart(){
        chart.getAxisLeft().setAxisMinimum(-0.5f);
        chart.getAxisLeft().setAxisMaximum(6.5f);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setInverted(true);

        chart.getAxisLeft().setDrawLabels(false);
//        chart.getXAxis().setDrawLabels(false);
//        chart.setExtraTopOffset(padding);
//        chart.setExtraBottomOffset(padding);
        chart.getLegend().setEnabled(false);
        chart.setContentDescription("");

        chart.getAxisRight().setEnabled(false);

//        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(24);
        chart.getXAxis().setDrawLabels(false);
        chart.getXAxis().setDrawAxisLine(false);

        chart.setNoDataText(analyticsFragment.getResources().getText(R.string.no_data_txt).toString());
        chart.setNoDataTextColor(ContextCompat.getColor(analyticsFragment.getContext(), R.color.colorPrimaryDark));
        chart.getDescription().setEnabled(false);

        chart.setOnChartValueSelectedListener(this);
    }

    private void setLegend(@NonNull TimeEventDataSet timeEveSet){
        for (TimeEventRange range: timeEveSet.getRangeList()) {
            String value = range.getStart().getName() + "→" + range.getEnd().getName();
            innerSetLegend(legendListForRange, range.getColorNum(), value);
        }

        for (TimeEvent event : timeEveSet.getEventList()) {
            innerSetLegend(legendListForTimeEve, event.getColorNum(), event.getName());
        }
    }

    private void innerSetLegend(List<Pair<Integer, String>> legendList, int colorNum, String value){
        Pair<Integer, String> pair = new Pair<>(colorNum, value);
        if (legendList.contains(pair))
            return;
        if (!legendList.contains(pair))
            legendList.add(pair);
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        int hour = (int)entry.getX();
        int min = Math.round((entry.getX() - hour) * 60);
        return time2String(hour, min);
    }

    @Override
    public void onScrollChanged() {
        int scrollX = hsv.getScrollX();
        if (listener != null) {
            listener.onScrollChanged(hsv, scrollX);
        }

        if (analyticsFragment.getView() == null) //時々NPEで落ちる
            return;
        VerticalViewPager vp = analyticsFragment.getView().findViewById(R.id.vertical_vp);
        if (vp == null) return;

//        scroll(vp.getCurrentItem()+2, scrollX);
//        scroll(vp.getCurrentItem()+1, scrollX);
//        scroll(vp.getCurrentItem()-1, scrollX);
//        scroll(vp.getCurrentItem()-2, scrollX);
        int currentPos = vp.getCurrentItem();
        for (int i = -OFFSET; i <= OFFSET; i++) {
            if (i == 0) continue;
            View item = analyticsFragment.getView().findViewWithTag(currentPos+i);
            if (item == null)
                return;

            HorizontalScrollView hsv = item.findViewById(R.id.scroll);
            scroll(hsv, scrollX);
        }
    }

    public void scroll(HorizontalScrollView hsv, int scrollX){
        hsv.getViewTreeObserver().removeOnScrollChangedListener(this);
        hsv.scrollTo(scrollX, 0);
        hsv.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    public HorizontalScrollView getHsv(){
        return hsv;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists()){
            Log.d(TAG, "onDataChange: !dataSnapshot.exists()" + dataSnapshot.getRef().getKey());
        } else {
            List<HashMap<String, Object>> list = ( List<HashMap<String, Object>>) dataSnapshot.getValue();
            Log.d(TAG, "onDataChange: ふにふに" + dataSnapshot.getRef().toString());//todo なにこれ？笑修正すること
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "onCancelled: " + databaseError.getMessage());
    }

    @OnClick(R2.id.up_btn)
    void wOnClickUpBtn(){
        if (listener != null)
            listener.onClickUpBtn();
    }

    @OnClick(R2.id.down_btn)
    void wOnClickDownBtn(){
        if (listener != null)
            listener.onClickDownBtn();
    }

    @Override
    public void onNothingSelected() {
        //do nothing
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(analyticsFragment.getContext(), (String)e.getData(), Toast.LENGTH_LONG).show();
    }

//    @Override
//    public void onScrollChange(View view, int x, int y, int oldx, int oldy) {
//        if (listener != null) {
//            listener.onScrollChanged((HorizontalScrollView) view, x, y, oldx, oldy);
//        }
//    }

    public List<Pair<Integer, String>> getLegendListForRange() {
        return legendListForRange;
    }

    public List<Pair<Integer, String>> getLegendListForTimeEve() {
        return legendListForTimeEve;
    }

    public void unbind(){
        unbinder.unbind();
    }
}

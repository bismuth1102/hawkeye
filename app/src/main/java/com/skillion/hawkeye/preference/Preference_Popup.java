package com.skillion.hawkeye.preference;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.skillion.detection.tracking.MultiBoxTracker;
import com.skillion.hawkeye.component.WhiteButton;
import com.skillion.hawkeye.component.WhiteButtonGroup;
import com.skillion.hawkeye.enums.SoundOption;
import com.skillion.hawkeye.env.Sound;
import com.skillion.hawkeye.env.ToastHelper;

import java.util.ArrayList;
import java.util.regex.Pattern;

import razerdp.basepopup.BasePopupWindow;

import static com.github.mikephil.charting.components.YAxis.YAxisLabelPosition.OUTSIDE_CHART;
import static com.skillion.hawkeye.enums.SoundOption.Beep;

public class Preference_Popup extends BasePopupWindow {

    Context context;
    float timeAlertNum, timeWarningNum, timeWatchNum;
    float ratioAlertNum, ratioWarningNum, ratioWatchNum;
    int seekbar1Num, seekbar2Num;

    SeekBar seekbar1, seekbar2;
    Button seekbarText1, seekbarText2;
    EditText timeAlertEdit, timeWarningEdit, timeWatchEdit;
    EditText ratioAlertEdit, ratioWarningEdit, ratioWatchEdit;
    SoundOption soundOption;

    private BarChart timeBarChart;
    //保存数据的实体（下面定义了两组数据集合）
    private ArrayList<BarEntry> timeEntries = new ArrayList<BarEntry>();

    private BarChart ratioBarChart;
    private ArrayList<BarEntry> ratioEntries = new ArrayList<BarEntry>();

    private final Preference_Controller controller;


    public Preference_Popup(Preference_Controller controller, Context context, float timeAlert,
                            float timeWarning, float timeWatch, float ratioAlert, float ratioWarning,
                            float ratioWatch, int seekbar1, int seekbar2, SoundOption s) {
        super(context);
        this.controller = controller;
        this.context = context;
        timeAlertNum = timeAlert;
        timeWarningNum = timeWarning;
        timeWatchNum = timeWatch;
        ratioAlertNum = ratioAlert;
        ratioWarningNum = ratioWarning;
        ratioWatchNum = ratioWatch;
        seekbar1Num = seekbar1;
        seekbar2Num = seekbar2;
        soundOption = s;
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.activity_preference);
    }

    public void init(){
        seekbar1 = findViewById(R.id.bar1_bar);
        seekbar2 = findViewById(R.id.bar2_bar);
        seekbarText1 = findViewById(R.id.bar1_time_img);
        seekbarText2 = findViewById(R.id.bar2_time_img);

        timeAlertEdit = findViewById(R.id.timeChart_alert_edit);
        timeWarningEdit = findViewById(R.id.timeChart_warning_edit);
        timeWatchEdit = findViewById(R.id.timeChart_watch_edit);

        ratioAlertEdit = findViewById(R.id.ratioChart_alert_edit);
        ratioWarningEdit = findViewById(R.id.ratioChart_warning_edit);
        ratioWatchEdit = findViewById(R.id.ratioChart_watch_edit);

        showTimeChart();
        showRatioChart();
        setTimeChartEditText();
        setRatioChartEditText();
        seekbarOnChange();
        volumeOnClick();
        defaultOnClick();
    }


    public void seekbarOnChange(){
        initSeekbarToCurrentVal();

        seekbarOnChangeImpl1(seekbar1, seekbarText1);
        seekbarOnChangeImpl2(seekbar2, seekbarText2);
    }

    private void initSeekbarToCurrentVal(){
        seekbar1.setProgress(seekbar1Num);
        seekbarText1.setText(seekbar1Num+" minutes");
        seekbar2.setProgress(seekbar2Num);
        seekbarText2.setText(seekbar2Num+" minutes");
    }


    private void seekbarOnChangeImpl1(SeekBar seekbar, Button seekbarText) {

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbar1Num = seekbar.getProgress();
                setSeekbar1Num();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarText.setText(progress+" minutes");

            }
        });
    }


    private void seekbarOnChangeImpl2(SeekBar seekbar, Button seekbarText) {

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbar2Num = seekbar.getProgress();
                setSeekbar2Num();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarText.setText(progress+" minutes");

            }
        });
    }


    public void volumeOnClick(){
        Sound sound = Sound.getInstance(context);

        WhiteButton beepBtn = findViewById(R.id.beep_btn);
        WhiteButton beelBtn = findViewById(R.id.beel_btn);
        WhiteButton chimeBtn = findViewById(R.id.chime_btn);
        WhiteButton offBtn = findViewById(R.id.off_btn);
        WhiteButtonGroup group = new WhiteButtonGroup(context, beelBtn, beepBtn, chimeBtn, offBtn);
        group.addGroupListener();

        switch (soundOption){
            case Off:
                offBtn.setClicked();
                break;
            case Beel:
                beelBtn.setClicked();
                break;
            case Beep:
                beepBtn.setClicked();
                break;
            case Chime:
                chimeBtn.setClicked();
                break;
            default:
                break;
        }

        offBtn.addClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sound.setAndStartSoundbyOption(SoundOption.Off);
            }
        });

        beepBtn.addClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sound.setAndStartSoundbyOption(SoundOption.Beep);
            }
        });

        beelBtn.addClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sound.setAndStartSoundbyOption(SoundOption.Beel);
            }
        });

        chimeBtn.addClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sound.setAndStartSoundbyOption(SoundOption.Chime);
            }
        });
    }


    public void showTimeChart(){
        timeBarChart = (BarChart) findViewById(R.id.time_barChart);
        timeEntries.add(new BarEntry(0f, timeAlertNum));
        timeEntries.add(new BarEntry(1f, timeWarningNum));
        timeEntries.add(new BarEntry(2f, timeWatchNum));

        //数据的集合（每组数据都需要一个数据集合存放数据实体和该组的样式）
        BarDataSet dataset = new BarDataSet(timeEntries, "");
        dataset.setColors(Color.RED, Color.YELLOW, Color.GREEN);
        dataset.setValueTextSize(0);
        BarData data = new BarData(dataset);
        data.setBarWidth(0.7f);

        XAxis xAxis = timeBarChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = timeBarChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMaximum(60);
        leftAxis.setAxisMinimum(0);
        leftAxis.setLabelCount(7, true);
        leftAxis.setTextSize(18);
        leftAxis.setPosition(OUTSIDE_CHART);
        leftAxis.setTextColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setGridColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setAxisLineColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setGridLineWidth(3);
        leftAxis.setAxisLineWidth(3);


        YAxis rightAxis = timeBarChart.getAxisRight();
        rightAxis.setEnabled(false);

        Description description = timeBarChart.getDescription();
        description.setTextColor(Color.TRANSPARENT);

        timeBarChart.setDescription(description);
        timeBarChart.setData(data);
        timeBarChart.invalidate();

        Button time_set_btn = (Button) findViewById(R.id.timeChart_set_btn);
        time_set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alertStr = timeAlertEdit.getText().toString();
                String warningStr = timeWarningEdit.getText().toString();
                String watchStr = timeWatchEdit.getText().toString();

                if (!timeValidate(alertStr) || !timeValidate(warningStr) || !timeValidate(watchStr)){
                    ToastHelper.showToast("should input an integer between 1 and 60", context);
                    return;
                }

                float alertF = Float.parseFloat(alertStr);
                float warningF = Float.parseFloat(warningStr);
                float watchF = Float.parseFloat(watchStr);

                if (alertF>=warningF){
                    ToastHelper.showToast("alert should smaller than warning", context);
                    return;
                }

                if (warningF>=watchF){
                    ToastHelper.showToast("warning should smaller than watch", context);
                    return;
                }

                timeAlertNum = alertF;
                timeWarningNum = warningF;
                timeWatchNum = watchF;
                changeTimeChartToCurrentVal();

                setTimeChartNum();

            }
        });
    }

    public void showRatioChart(){
        ratioBarChart = (BarChart) findViewById(R.id.ratio_barChart);
        ratioEntries.add(new BarEntry(0f, ratioAlertNum));
        ratioEntries.add(new BarEntry(1f, ratioWarningNum));
        ratioEntries.add(new BarEntry(2f, ratioWatchNum));

        //数据的集合（每组数据都需要一个数据集合存放数据实体和该组的样式）
        BarDataSet dataset = new BarDataSet(ratioEntries, "");
        dataset.setColors(Color.RED, Color.YELLOW, Color.GREEN);
        dataset.setValueTextSize(0);
        BarData data = new BarData(dataset);
        data.setBarWidth(0.7f);

        XAxis xAxis = ratioBarChart.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = ratioBarChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMaximum(50);
        leftAxis.setAxisMinimum(0);
        leftAxis.setLabelCount(6, true);
        leftAxis.setTextSize(18);
        leftAxis.setPosition(OUTSIDE_CHART);
        leftAxis.setTextColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setGridColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setAxisLineColor(context.getResources().getColor(R.color.skillionBlue));
        leftAxis.setGridLineWidth(3);
        leftAxis.setAxisLineWidth(3);


        YAxis rightAxis = ratioBarChart.getAxisRight();
        rightAxis.setEnabled(false);

        Description description = ratioBarChart.getDescription();
        description.setTextColor(Color.TRANSPARENT);

        ratioBarChart.setDescription(description);
        ratioBarChart.setData(data);
        ratioBarChart.invalidate();

        Button ratio_set_btn = (Button) findViewById(R.id.ratioChart_set_btn);
        ratio_set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alertStr = ratioAlertEdit.getText().toString();
                String warningStr = ratioWarningEdit.getText().toString();
                String watchStr = ratioWatchEdit.getText().toString();

                if (!ratioValidate(alertStr) || !ratioValidate(warningStr) || !ratioValidate(watchStr)){
                    ToastHelper.showToast("should input an integer between 1 and 50", context);
                    return;
                }

                float alertF = Float.parseFloat(alertStr);
                float warningF = Float.parseFloat(warningStr);
                float watchF = Float.parseFloat(watchStr);

                MultiBoxTracker.alertScope = alertF/100;
                MultiBoxTracker.warningScope = warningF/100;
                MultiBoxTracker.watchScope = watchF/100;

                if (alertF<=warningF){
                    ToastHelper.showToast("alert should bigger than warning", context);
                    return;
                }

                if (warningF<=watchF){
                    ToastHelper.showToast("warning should bigger than watch", context);
                    return;
                }

                ratioAlertNum = alertF;
                ratioWarningNum = warningF;
                ratioWatchNum = watchF;
                changeRatioChartToCurrentVal();

                setRatioChartNum();

            }
        });
    }

    private void changeTimeChartToCurrentVal(){
        timeEntries.clear();
        timeEntries.add(new BarEntry(0f, timeAlertNum));
        timeEntries.add(new BarEntry(1f, timeWarningNum));
        timeEntries.add(new BarEntry(2f, timeWatchNum));
        timeBarChart.invalidate();
    }


    private void changeRatioChartToCurrentVal(){
        ratioEntries.clear();
        ratioEntries.add(new BarEntry(0f, ratioAlertNum));
        ratioEntries.add(new BarEntry(1f, ratioWarningNum));
        ratioEntries.add(new BarEntry(2f, ratioWatchNum));
        ratioBarChart.invalidate();
    }

    public void setTimeChartEditText(){
        timeAlertEdit.setText(String.valueOf((int) timeAlertNum));
        timeWarningEdit.setText(String.valueOf((int) timeWarningNum));
        timeWatchEdit.setText(String.valueOf((int) timeWatchNum));
    }

    public void setRatioChartEditText(){
        ratioAlertEdit.setText(String.valueOf((int) ratioAlertNum));
        ratioWarningEdit.setText(String.valueOf((int) ratioWarningNum));
        ratioWatchEdit.setText(String.valueOf((int) ratioWatchNum));
    }


    private static boolean timeValidate(String str) {
        String pattern = "60|[1-5]?[0-9]";
        boolean isMatch = Pattern.matches(pattern, str);
        return isMatch;
    }

    private static boolean ratioValidate(String str) {
        String pattern = "50|[1-4]?[0-9]";
        boolean isMatch = Pattern.matches(pattern, str);
        return isMatch;
    }

    private void defaultOnClick(){
        Button defaultBtn = findViewById(R.id.default_btn);
        defaultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar1Num = 40;
                seekbar2Num = 40;
                timeAlertNum = 20;
                timeWarningNum = 40;
                timeWatchNum = 60;

                setSeekbar1Num();
                setSeekbar2Num();
                setTimeChartNum();
                setRatioChartNum();

                initSeekbarToCurrentVal();
                changeTimeChartToCurrentVal();
                changeRatioChartToCurrentVal();
                setTimeChartEditText();
                setRatioChartEditText();
            }
        });
    }


    private void setSeekbar1Num(){
        controller.setSeekbar1Num(seekbar1Num);
    }

    private void setSeekbar2Num(){
        controller.setSeekbar2Num(seekbar2Num);
    }

    private void setTimeChartNum(){
        controller.setTimeChartNum(timeAlertNum, timeWarningNum, timeWatchNum);
    }

    private void setRatioChartNum(){
        controller.setRatioChartNum(ratioAlertNum, ratioWarningNum, ratioWatchNum);
    }

}
package com.skillion.hawkeye.preference;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.skillion.hawkeye.enums.SoundOption;
import com.skillion.hawkeye.env.Sound;
import com.skillion.hawkeye.settings.Settings_Popup;

import razerdp.basepopup.BasePopupWindow;

public class Preference_Controller {
    private float timeAlertNum = 20;
    private float timeWarningNum = 40;
    private float timeWatchNum = 60;
    private float ratioAlertNum = 30;
    private float ratioWarningNum = 20;
    private float ratioWatchNum = 10;

    private int seekbar1Num = 40;
    private int seekbar2Num = 40;
    private SoundOption sound = SoundOption.Beep;
    private static final Preference_Controller instance = new Preference_Controller();


    private Preference_Controller(){ }

    public static Preference_Controller getInstance(){
        return instance;
    }


    public void showPreference(View v) {
//        Animation showAnimation = AnimationHelper.asAnimation()
//                .withScale(ScaleConfig.CENTER)
//                .toShow();
//        Animation dismissAnimation = AnimationHelper.asAnimation()
//                .withScale(ScaleConfig.CENTER)
//                .toDismiss();
        sound = Sound.getInstance(v.getContext()).getSoundOption();
        Preference_Popup popup = new Preference_Popup(this, v.getContext(),
                timeAlertNum, timeWarningNum, timeWatchNum,
                ratioAlertNum, ratioWarningNum, ratioWatchNum,
                seekbar1Num, seekbar2Num, sound);
        popup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setPopupGravity(Gravity.TOP);
//        popup.setShowAnimation(showAnimation);
//        popup.setDismissAnimation(dismissAnimation);
        popup.setBackgroundColor(Color.TRANSPARENT);

        popup.showPopupWindow(v);
        popup.init();

    }


    public void showVolume(View v){
        BasePopupWindow.GravityMode horizontalGravityMode = BasePopupWindow.GravityMode.RELATIVE_TO_ANCHOR;
        BasePopupWindow.GravityMode verticalGravityMode = BasePopupWindow.GravityMode.RELATIVE_TO_ANCHOR;

        Settings_Popup popup = new Settings_Popup(v.getContext());
        popup.setPopupGravityMode(horizontalGravityMode, verticalGravityMode);
        popup.setPopupGravity(Gravity.END | Gravity.TOP);
//        popup.setShowAnimation(showAnimation);
//        popup.setDismissAnimation(dismissAnimation);
        popup.setBackgroundColor(Color.TRANSPARENT);

        popup.showPopupWindow(v);
    }

    public void setTimeChartNum(float alert, float warning, float watch){
        timeAlertNum = alert;
        timeWarningNum = warning;
        timeWatchNum = watch;
    }

    public void setRatioChartNum(float alert, float warning, float watch){
        ratioAlertNum = alert;
        ratioWarningNum = warning;
        ratioWatchNum = watch;
    }

    public void setSeekbar1Num(int i){
        seekbar1Num = i;
    }

    public void setSeekbar2Num(int i){
        seekbar2Num = i;
    }

}

package com.skillion.hawkeye.settings;

import android.content.Context;
import android.view.View;

import com.R;

import razerdp.basepopup.BasePopupWindow;

public class Settings_Popup extends BasePopupWindow {
    Context context;


    public Settings_Popup(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateContentView() {
        return createPopupById(R.layout.volume_change);
    }

}

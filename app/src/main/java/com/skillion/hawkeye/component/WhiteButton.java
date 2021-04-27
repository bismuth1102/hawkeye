package com.skillion.hawkeye.component;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

import com.R;

import java.util.ArrayList;
import java.util.List;

public class WhiteButton extends AppCompatButton {

    List<OnClickListener> list = new ArrayList<>();

    public WhiteButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhiteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        addClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setBackground(getResources().getDrawable(R.drawable.button_blue));
                setTextColor(Color.WHITE);
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                WhiteButton.this.onClick();
                for(OnClickListener listener: list){
                    listener.onClick(v);
                }
            }
        });
    }

    public void addClickListener(OnClickListener listener){
        list.add(listener);
    }

    public void setClicked(){
        setBackground(getResources().getDrawable(R.drawable.button_blue));
        setTextColor(Color.WHITE);
    }

}

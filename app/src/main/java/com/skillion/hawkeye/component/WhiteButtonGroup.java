package com.skillion.hawkeye.component;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.R;

import java.util.ArrayList;
import java.util.Arrays;

public class WhiteButtonGroup extends ArrayList {

    ArrayList<WhiteButton> list;
    Context context;

    public WhiteButtonGroup(Context _context, WhiteButton ... buttons){
        list = new ArrayList<>();
        list.addAll(Arrays.asList(buttons));
        context = _context;
    }

    public void addGroupListener(){
        for (int i=0; i<list.size(); i++){
            WhiteButton button = list.get(i);
            int finalI = i;

            button.addClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int j=0; j<list.size(); j++){
                        if (j== finalI) continue;
                        list.get(j).setBackground(context.getResources().getDrawable(R.drawable.button_white));
                        list.get(j).setTextColor(context.getResources().getColor(R.color.skillionBlue));
                    }
                }
            });
        }

    }


}

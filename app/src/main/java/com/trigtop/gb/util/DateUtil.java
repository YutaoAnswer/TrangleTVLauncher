package com.trigtop.gb.util;

import android.icu.text.SimpleDateFormat;

import java.util.Date;

public class DateUtil {

    public static String getSystemDate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss EEEE");
            Date date = new Date(System.currentTimeMillis());
            return simpleDateFormat.format(date);
        }
        return null;
    }

}

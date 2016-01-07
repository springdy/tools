package com.springdy.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by springdy on 2015/12/26.
 */
public class DateFormat {
    public static Date parse(String value,String format) throws ParseException {
        SimpleDateFormat format1 = new SimpleDateFormat(format);
        return format1.parse(value);
    }
}

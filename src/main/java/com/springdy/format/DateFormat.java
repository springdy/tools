package com.springdy.format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by springdy on 2015/12/26.
 */
public class DateFormat {
    private static ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>> DATE_FORMAT_MAP = new ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>(10);

    public static SimpleDateFormat getDateFormat(final String format){
        ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = DATE_FORMAT_MAP.get(format);
        if(null == dateFormatThreadLocal){
            dateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>(){
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat(format);
                }
            };
            DATE_FORMAT_MAP.put(format,dateFormatThreadLocal);
        }
        return dateFormatThreadLocal.get();
    }

    public static Date parse(String value,final String format) throws ParseException {
        return getDateFormat(format).parse(value);
    }

    public static String format(Date date,final String format){
        return getDateFormat(format).format(date);
    }

}

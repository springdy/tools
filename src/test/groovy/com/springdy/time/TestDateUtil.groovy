package com.springdy.time

/**
 * Created by springdy on 2016/9/16.
 */
class TestDateUtil extends GroovyTestCase{

    void test(){
        Date date = DateUtil.getDate(2015,12,1)
        assertEquals("2015-12-01",DateUtil.format(date,"yyyy-MM-dd"))
        assertEquals("2015-12-01 00:00:00",DateUtil.format(date,"yyyy-MM-dd HH:mm:ss"))
        Date date2 = DateUtil.getDate(2015,12,1,12,12,12)
        assertEquals("2015-12-01 12:12:12",DateUtil.format(date2,"yyyy-MM-dd HH:mm:ss"))
        assertEquals("2015-12-01 12:12:12:000",DateUtil.format(date2,"yyyy-MM-dd HH:mm:ss:SSS"))
    }
}

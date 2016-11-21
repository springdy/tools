package com.springdy.http;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;

/**
 * Created by springdy on 2016/6/1.
 */
public class HttpUtils {

    /**
     * @parma httpProxy 代理ip
     * @parma timeout 超时时间 单位 ms
    */
    public static RequestConfig getRequestConfig(HttpHost httpProxy, int timeout){
        return
            RequestConfig.custom().setProxy(httpProxy).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).setConnectTimeout(timeout).build();
    }

    /**
     * @parma timeout 超时时间，单位 ms
     */
    public static RequestConfig getRequestConfig(int timeout){
        return
            RequestConfig.custom().setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).setConnectTimeout(timeout).build();
    }
}

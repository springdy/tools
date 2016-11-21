package com.springdy.http;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by springdy on 2016/4/14.
 */
public class HttpClients {
    private static PoolingHttpClientConnectionManager connectionManager;
    private static RequestConfig requestConfigDefault;

    static {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(1500);
        connectionManager.setDefaultMaxPerRoute(1500);
        requestConfigDefault = RequestConfig.custom().build();
    }

    public static HttpClient getDefaultClient() {
        return org.apache.http.impl.client.HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfigDefault).setRetryHandler(new DefaultHttpRequestRetryHandler(2, false)).build();
    }

    public static HttpClient getDefaultClient(CookieStore cookieStore) {
        return org.apache.http.impl.client.HttpClients.custom().setConnectionManager(connectionManager).setRetryHandler(new DefaultHttpRequestRetryHandler(2, false)).setDefaultCookieStore(cookieStore).build();
    }
}

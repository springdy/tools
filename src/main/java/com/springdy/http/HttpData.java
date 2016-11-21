package com.springdy.http;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.util.Map;

/**
 * Created by springdy on 2016/4/19.
 * http请求类数据类
 */
public class HttpData {
    private CookieStore cookieStore;
    private HttpClient httpClient;

    /**
     * @parma cookieStore cooke容器
     * @parma 链接超时时间
     */
    public HttpData(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        //暂时定义超市时间，是10s
        httpClient = HttpClients.getDefaultClient(cookieStore);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    /**
     * 不允许外部在重新定义
     */
    private void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public void initCookies(String cookieDomain, String cookieStr) {
        for (String str : cookieStr.split(";")) {
            int i = str.indexOf("=");
            BasicClientCookie cookie = null;
            if (i > 0) {
                cookie =
                        new BasicClientCookie(str.substring(0, i), str.substring(i + 1, str.length()));
            } else {
                cookie = new BasicClientCookie(str, "");
            }
            cookie.setDomain(cookieDomain);
            cookieStore.addCookie(cookie);
        }
    }
}

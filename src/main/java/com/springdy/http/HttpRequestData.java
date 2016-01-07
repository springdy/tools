package com.springdy.http;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 在需要换ip的时候，不建议重新new一个该对象，建议使用以下方法
 *
 * @author kafka
 */
public class HttpRequestData implements Serializable {
    private static final long serialVersionUID = 154983995789725361L;
    private static final Logger log = LoggerFactory.getLogger(HttpRequestData.class);
    private Map<String, String> headers;
    private Map<String, String> responseHeaders;
    private transient HttpClient httpClient;
    private String url;

    /**
     * 不常用字段
     * 用于表示用户取验证码是否获取成功，某些银行可能需要验证码，也可能不需要验证码，此时，该字段起作用
     */
    private boolean imgSuccess = false;

    public HttpRequestData() {
        initHttpClient();
    }

    public Map<String, String> getResponseHeaders() {
        if (null == responseHeaders)
            responseHeaders = new HashMap<String, String>();
        return responseHeaders;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setResponseHeaders(Header[] headers) {
        if (null == responseHeaders)
            responseHeaders = new HashMap<String, String>();
        responseHeaders.clear();
        for (Header header : headers) {
            responseHeaders.put(header.getName(), header.getValue());
        }
    }

    public Map<String, String> getHeaders() {
        if (null == headers)
            headers = new HashMap<String, String>();
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 获取httpClient
     * <p/>
     * 如果需要并发使用httpClient，务必记得使用MultiThreadedHttpConnectionManager
     * 如果有需要的话，记得设置cookie，参见方法initHttpClientCookie
     * <p/>
     * 在已经使用了getHttpClient方法后，则该类只会有这个不变的HttpClient对象，此时使用useProxy将不会产生任何效果
     */
    private void initHttpClient() {
        httpClient = new HttpClient(new HttpClientParams(), new MultiThreadedHttpConnectionManager());// 连接在releaseConnection后总是被关闭
        try {
            // httpClient.getParams().setAuthenticationPreemptive(true);
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            httpClient.getParams().setSoTimeout(10000);// 10秒超时response
            httpClient.getParams().setConnectionManagerTimeout(10000);// 10秒超时connect
            httpClient.getHttpConnectionManager().getParams().setSoTimeout(10000);
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取httpClient 默认使用代理ip
     * <p/>
     * 如果需要并发使用httpClient，务必记得使用MultiThreadedHttpConnectionManager
     * 如果有需要的话，记得设置cookie，参见方法initHttpClientCookie
     * 如果事先没有创建返回唯空，此方法前必须要创建
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 当准备使用httpClient提取数据，而httpClient没有对应的cookie设置时，请务必调用此方法。
     */
    public void initHttpClientCookie(String domain, Map<String, String> cookies) {
        if (null == cookies || cookies.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            getHttpClient().getState().addCookie(new Cookie(domain, entry.getKey(), entry.getValue(), "/", null, false));
        }
    }

    /**
     * 当准备使用httpClient提取数据，而httpClient没有对应的cookie设置时，请务必调用此方法。
     */
    public void initHttpClientCookie(String domain, String cookies) {
        if (null == cookies || cookies.isEmpty()) {
            return;
        }
        for (String x : cookies.split(";")) {
            getHttpClient().getState().addCookie(new Cookie(domain, StringUtils.substringBefore(x, "=").trim(), StringUtils.substringAfter(x, "=").trim(), "/", null, false));
        }
    }

    /**
     * 获取当前的cookies的值
     *
     * @return cookie的字符串
     */
    public String getCookieStr(String domain) {
        StringBuilder buffer = new StringBuilder();
        Map<String,String> cookieMap = getCookieMap(domain);
        for(Map.Entry<String,String> entry:cookieMap.entrySet()){
            buffer.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return buffer.toString();
    }

    /**
     * 获取当前的cookies的值
     *
     * @return cookie的字符串
     */
    public Map<String, String> getCookieMap(String domain) {
        Map<String, String> cookieMap = new HashMap<String, String>();
        for (Cookie c : getHttpClient().getState().getCookies()) {
            if(c.getValue().contains(domain)) {
                cookieMap.put(c.getName(), c.getValue());
            }
        }
        return cookieMap;
    }

    /**
     * 获取当前的cookies的值
     *
     * @return cookie的字符串
     */
    public String getCookieValue(String name) {
        Map<String, String> cookieMap = new HashMap<String, String>();
        for (Cookie c : getHttpClient().getState().getCookies()) {
           if(c.getName().equals(name)){
               return c.getValue();
           }
        }
        return null;
    }


    // 相关序列化需要等测试
    private void readObject(ObjectInputStream is) throws ClassNotFoundException, IOException {
        is.defaultReadObject();
        getHttpClient().setParams((HttpClientParams) is.readObject());
        getHttpClient().getState().addCookies((Cookie[]) is.readObject());
        // TODO 序列化httpclient
    }

    private void writeObject(ObjectOutputStream os) throws IOException {
        os.defaultWriteObject();
        os.writeObject(httpClient.getParams());
        os.writeObject(httpClient.getState().getCookies());
        // TODO 反序列化httpclient
    }

    private Object readResolve() {
        return this;
    }

    /**
     *
     */
    public HttpState getHttpState() {
        return this.getHttpClient().getState();
    }

}

package com.springdy.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by springdy on 2016/4/14.
 */
public class FetchUtils {
    //默认的编码
    public final static String DEFAOUT_CHARSET = "UTF-8";
    private final static String DEFAOUT_CONTENTTYPE = "text/html";
    private final static Map<String, String> DEAULT_HEAHDERMAP = new HashMap<>();

    static {
        //支持压缩
        DEAULT_HEAHDERMAP.put("Accept-Encoding", "gzip");
        DEAULT_HEAHDERMAP.put("Accept", "text/html, application/xhtml+xml, image/jxr, */*");
        DEAULT_HEAHDERMAP.put("Accept-Language", "zh-Hans-CN,zh-Hans;q=0.5");
        DEAULT_HEAHDERMAP.put("Content-Type", "application/x-www-form-urlencoded");
        //chrome
        DEAULT_HEAHDERMAP.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.110 Safari/537.36");
    }

    public static String get(String url) throws IOException {
        return get(HttpClients.getDefaultClient(), url, null, null);
    }

    public static String get(String url, RequestConfig requestConfig) throws IOException {
        return get(HttpClients.getDefaultClient(), url, requestConfig, null);
    }

    public static String get(String url, Map<String, String> inputs, RequestConfig requestConfig) throws IOException {
        url = getInputsUrl(url, inputs);
        return get(HttpClients.getDefaultClient(), url, requestConfig, null);
    }

    public static String get(String url, Map<String, String> inputs) throws IOException {
        url = getInputsUrl(url, inputs);
        return get(HttpClients.getDefaultClient(), url, null, null);
    }

    public static String get(HttpClient client, String url, Map<String, String> inputs) throws IOException {
        url = getInputsUrl(url, inputs);
        return get(client, url, null, null);
    }

    public static String get(HttpClient client, String url) throws IOException {
        return get(client, url, null, null);
    }

    private static String getInputsUrl(String url, Map<String, String> inputs) throws UnsupportedEncodingException {
        if (null != inputs) {
            StringBuilder urlBuilder = new StringBuilder(url);
            boolean first = true;
            // reconstitute the query, ready for appends
            if (!url.contains("?"))
                urlBuilder.append("?");
            for (Map.Entry<String, String> entry : inputs.entrySet()) {
                if (!first)
                    urlBuilder.append('&');
                else
                    first = false;
                urlBuilder.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), DEFAOUT_CHARSET));
            }
            url = urlBuilder.toString();
        }
        return url;
    }

    public static String get(String url, Map<String, String> inputs, RequestConfig requestConfig, Map<String, String> headers) throws IOException {

        return get(HttpClients.getDefaultClient(), url, requestConfig, headers);
    }

    public static String get(String url, RequestConfig requestConfig, Map<String, String> headers) throws IOException {
        return get(HttpClients.getDefaultClient(), url, requestConfig, headers);
    }

    public static String get(HttpClient client, String url, RequestConfig requestConfig, Map<String, String> headers) throws IOException {
        String responseContent = null;
        HttpGet get = null;
        try {
            get = new HttpGet(url);
            if (null != requestConfig) {
                get.setConfig(requestConfig);
            }
            if (null == headers) {
                headers = DEAULT_HEAHDERMAP;
            } else {
                //如果请求头没有默认值，使用默认值
                for (Map.Entry<String, String> entry : DEAULT_HEAHDERMAP.entrySet()) {
                    if (!headers.containsKey(entry.getKey())) {
                        headers.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            HeaderGroup headerGroup = new HeaderGroup();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headerGroup.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            get.setHeaders(headerGroup.getAllHeaders());
            HttpResponse response = client.execute(get);
            responseContent = EntityUtils.toString(response.getEntity());
            // 确保实体内容被完全消耗
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (ClientProtocolException e) {
            throw new ClientProtocolException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            if (null != get) {
                get.releaseConnection();
            }
        }
        return responseContent;
    }

    public static String post(String url, Map<String, String> inputs) throws IOException {
        return post(HttpClients.getDefaultClient(), url, inputs, null, null);
    }

    public static String post(String url, Map<String, String> inputs, RequestConfig requestConfig) throws IOException {
        return post(HttpClients.getDefaultClient(), url, inputs, requestConfig, null);
    }

    public static String post(String url, Map<String, String> inputs, Map<String, String> headers) throws IOException {
        return post(HttpClients.getDefaultClient(), url, inputs, null, headers);
    }

    public static String post(HttpClient client, String url, Map<String, String> inputs) throws IOException {
        return post(client, url, inputs, null, null);
    }

    public static String post(HttpClient client, String url) throws IOException {
        return post(client, url, new HashMap<>(), null, null);
    }

    public static String post(HttpClient client, String url, Map<String, String> inputs, Map<String, String> headers) throws IOException {
        return post(client, url, inputs, null, headers);
    }


    public static String post(String url, String requst) throws IOException {
        return post(HttpClients.getDefaultClient(), url, new StringEntity(requst, "utf-8"), null, null);
    }

    public static String post(String url, String requst, RequestConfig requestConfig) throws IOException {
        return post(HttpClients.getDefaultClient(), url, new StringEntity(requst, "utf-8"), requestConfig, null);
    }

    public static String post(String url, String requst, Map<String, String> headers) throws IOException {
        return post(HttpClients.getDefaultClient(), url, new StringEntity(requst, "utf-8"), null, headers);
    }

    public static String post(String url, String requst, Map<String, String> headers, RequestConfig requestConfig) throws IOException {
        return post(HttpClients.getDefaultClient(), url, new StringEntity(requst, "utf-8"), requestConfig, headers);
    }

    public static String post(HttpClient client, String url, String requst) throws IOException {
        return post(client, url, new StringEntity(requst, "utf-8"), null, null);
    }

    public static String post(HttpClient client, String url, String requst, Map<String, String> headers) throws IOException {
        return post(client, url, new StringEntity(requst, "utf-8"), null, headers);
    }

    public static String post(String url, HttpEntity httpEntity, RequestConfig requestConfig, Map<String, String> headers) throws IOException {
        return post(HttpClients.getDefaultClient(), url, httpEntity, requestConfig, headers);
    }

    public static String post(HttpClient client, String url, HttpEntity httpEntity, RequestConfig requestConfig, Map<String, String> headers) throws IOException {
        String responseContent = null;
        HttpPost post = null;
        try {
            post = new HttpPost(url);
            if (null != requestConfig) {
                post.setConfig(requestConfig);
            }
            if (null == headers) {
                headers = DEAULT_HEAHDERMAP;
            } else {
                //如果请求头没有默认值，使用默认值
                for (Map.Entry<String, String> entry : DEAULT_HEAHDERMAP.entrySet()) {
                    if (!headers.containsKey(entry.getKey())) {
                        headers.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            HeaderGroup headerGroup = new HeaderGroup();
            headers.remove("Content-Type", "application/x-www-form-urlencoded");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headerGroup.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            post.setHeaders(headerGroup.getAllHeaders());
            post.setEntity(httpEntity);
            HttpResponse response = client.execute(post);
            responseContent = EntityUtils.toString(response.getEntity());
            // 确保实体内容被完全消耗
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (ClientProtocolException e) {
            throw new ClientProtocolException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            post.releaseConnection();
        }
        return responseContent;
    }

    public static String post(HttpClient client, String url, Map<String, String> inputs, RequestConfig requestConfig, Map<String, String> headers) throws IOException {
        String responseContent = null;
        HttpPost post = null;
        try {
            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            if (null != inputs) {
                for (Map.Entry<String, String> entry : inputs.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            post = new HttpPost(url);
            if (null != requestConfig) {
                post.setConfig(requestConfig);
            }
            if (null == headers) {
                headers = DEAULT_HEAHDERMAP;
            } else {
                //如果请求头没有默认值，使用默认值
                for (Map.Entry<String, String> entry : DEAULT_HEAHDERMAP.entrySet()) {
                    if (!headers.containsKey(entry.getKey())) {
                        headers.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            HeaderGroup headerGroup = new HeaderGroup();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                headerGroup.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            post.setHeaders(headerGroup.getAllHeaders());
            post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
            HttpResponse response = client.execute(post);
            responseContent = EntityUtils.toString(response.getEntity());
            // 确保实体内容被完全消耗
            EntityUtils.consumeQuietly(response.getEntity());
        } catch (ClientProtocolException e) {
            throw new ClientProtocolException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            post.releaseConnection();
        }
        return responseContent;
    }
}

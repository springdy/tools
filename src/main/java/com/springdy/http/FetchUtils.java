package com.springdy.http;

import com.springdy.regex.RegexUtil;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

public final class FetchUtils {
    private static final Logger log = LoggerFactory.getLogger(FetchUtils.class);
    private static int size = 1 << 23;
    private static int DEFAOUT_TIMEOUT = 10000;
    private static String DEFAOUT_CHARSET = "utf-8";

    /**
     * 初始化httpMethod
     */
    private static void initHttpMethod(HttpMethod method, Map<String, String> headers) throws URIException {
        if (null != headers) {// 添加请求头参数
            if (!headers.containsKey("User-Agent")) {// 无ua，默认为ie8
                method.addRequestHeader("User-Agent", UserAgent.WIN_CHROME);
            }
            if (!headers.containsKey("Accept-Encoding")) {// 无ua，默认为ie8
                method.addRequestHeader("Accept-Encoding", "gzip");
            }
            for (Entry<String, String> entry : headers.entrySet()) {
                method.addRequestHeader(entry.getKey(), entry.getValue());
            }
        } else {
            //默认的请求头
            method.addRequestHeader("User-Agent", UserAgent.WIN_CHROME);
            //支持gzip解压
            method.addRequestHeader("Accept-Encoding", "gzip");

        }
        method.getParams().setVersion(HttpVersion.HTTP_1_1);
        method.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
        method.getParams().setSoTimeout(DEFAOUT_TIMEOUT);// 10秒超时response
        method.setFollowRedirects(false);
    }

    /**
     *
     */
    private static String executeMethod(HttpMethod method, String url, HttpRequestData data) throws IOException {
        data.getHttpClient().executeMethod(method);
        data.setResponseHeaders(method.getResponseHeaders());
        Header header = method.getResponseHeader("Location");
        if (null != header) {
            String location = header.getValue();
            String targetUrl = completeUrl(url, location);
            targetUrl = targetUrl.replaceAll(" ", "");
            return get(data, targetUrl);
        }
        String charset = null;
        String html = null;
        String contentType = method.getResponseHeader("Content-Type").getValue();
        charset = RegexUtil.singleExtract(contentType, "charset=([\\w-\\d]+)", 1);
        if (null == charset) {
            charset = DEFAOUT_CHARSET;
        }
        Header ceHeader = method.getResponseHeader("Content-Encoding");
        long contentLen = 0l;
        if (null != method.getRequestHeader("Content-Length")) {
            contentLen = Long.valueOf(method.getRequestHeader("Content-Length").getValue());
        }
        if (null != ceHeader) {
            if (ceHeader.getValue().contains("gzip")) {
                byte[] bytes = null;
                return inputStreamAsString(new GZIPInputStream(method.getResponseBodyAsStream()), charset, contentLen);
            }
        }
        html = inputStreamAsString(method.getResponseBodyAsStream(), charset, contentLen);
        return html;
    }

    /**
     * 将数据流处理成字符串
     * 处理网页编码问题
     */
    private static String inputStreamAsString(InputStream in, String charset, long contentLength) {
        try {
            if (null == charset) {
                charset = DEFAOUT_CHARSET;
            }
            int len = 0;
            byte[] buffer = new byte[1024];
            len = in.read(buffer);
            String cs = RegexUtil.singleExtract(new String(buffer, 0, len), "charset=([\\w-\\d]+)", 1);
            if (null != cs) {
                charset = cs;
            }
            ByteArrayOutputStream outstream = new ByteArrayOutputStream(contentLength > 0L ? (int) contentLength : 1024);
            outstream.write(buffer, 0, len);
            if (len > 0) {
                while ((len = in.read(buffer)) > 0) {
                    outstream.write(buffer, 0, len);
                }
            }
            outstream.close();
            return new String(outstream.toByteArray(), charset);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * url中特殊处理
     */
    public static String urlSpecialChar(String value) {
        return value.replaceAll("&amp;", "&");
    }

    /**
     * http get 字符数组
     */
    public static byte[] getBytes(HttpRequestData data, String url) throws IOException {
        url = urlSpecialChar(url);
        GetMethod get = new GetMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(get, data.getHeaders());
            httpClient.executeMethod(get);
            trace(httpClient, url, null);
            data.setResponseHeaders(get.getResponseHeaders());
            return get.getResponseBody(size);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static byte[] getBytes(HttpRequestData data, String url, Map<String, String> input) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(url);
        boolean first = true;
        if (!url.contains("?"))
            urlBuilder.append("?");
        for (Entry<String, String> entry : input.entrySet()) {
            if (!first)
                urlBuilder.append('&');
            else
                first = false;
            urlBuilder.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), DEFAOUT_CHARSET));
        }
        url = urlBuilder.toString();
        return getBytes(data, url);
    }

    public static byte[] postBytes(HttpRequestData data, String url) throws IOException {
        return postBytes(data, url, null);

    }

    public static byte[] postBytes(HttpRequestData data, String url, Map<String, String> inputs) throws IOException {
        // 默认10秒
        return postBytes(data, url, inputs, DEFAOUT_TIMEOUT);
    }

    public static byte[] postBytes(HttpRequestData data, String url, Map<String, String> inputs, Map<String, String> headers) throws IOException {
        // 默认10秒
        return postBytes(data, url, inputs, DEFAOUT_TIMEOUT, headers);
    }

    public static byte[] postBytes(HttpRequestData data, String url, Map<String, String> inputs, int milliseconds) throws IOException {
        return postBytes(data, url, inputs, milliseconds, data.getHeaders());
    }

    public static byte[] postBytes(HttpRequestData data, String url, Map<String, String> inputs, int milliseconds, Map<String, String> headers) throws IOException {
        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(post, headers);
            post.getParams().setSoTimeout(milliseconds);// 设置超时
            if (null != inputs) {
                for (Entry<String, String> entry : inputs.entrySet()) {
                    post.addParameter(entry.getKey(), entry.getValue());
                }
            }
            httpClient.executeMethod(post);

            trace(httpClient, url, null);

            return post.getResponseBody(size);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static byte[] postBytes2(HttpRequestData data, String url, String encode) throws IOException {
        return postBytes2(data, url, encode, null);
    }

    public static byte[] postBytes2(HttpRequestData data, String url, String encode, Map<String, String> inputs) throws IOException {
        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.getHttpClient();
        StringBuilder postData = new StringBuilder();
        try {
            if (null != inputs) {
                for (Entry<String, String> entry : inputs.entrySet()) {
                    postData.append(URLEncoder.encode(entry.getKey(), encode)).append("=").append(URLEncoder.encode(entry.getValue(), encode)).append("&");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            initHttpMethod(post, null);
            RequestEntity entity = new StringRequestEntity(postData.toString(), "text/html", encode);
            post.setRequestEntity(entity);
            httpClient.executeMethod(post);

            trace(httpClient, url, null);

            return post.getResponseBody(size);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http get调用
     */
    public static String get(HttpRequestData data, String url) throws IOException {
        return get(data, url, null);
    }

    /**
     * http get调用
     */
    public static String get(HttpRequestData data, String url, String charset) throws IOException {
        url = urlSpecialChar(url);
        data.setUrl(url);
        GetMethod get = new GetMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(get, null);
            return executeMethod(get, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http get调用
     */
    public static String get(HttpRequestData data, String url, int milliseconds) throws IOException {
        url = urlSpecialChar(url);
        GetMethod get = new GetMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(get, null);
            get.getParams().setSoTimeout(milliseconds);// 设置超时
            return executeMethod(get, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            get.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * 对url的特殊字符特殊处理u
     *
     * @param value 待处理的字符串
     * @return 返回处理后的字符串
     */
    public static String urlFormat(String value) {
        return value.replaceAll("&amp;", "&");
    }

    public static String get(HttpRequestData data, String url, Map<String, String> input, String charset, int milliseconds) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(url);
        boolean first = true;
        // reconstitute the query, ready for appends
        if (!url.contains("?"))
            urlBuilder.append("?");
        for (Entry<String, String> entry : input.entrySet()) {
            if (!first)
                urlBuilder.append('&');
            else
                first = false;
            urlBuilder.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), charset));
        }
        url = urlBuilder.toString();
        return get(data, url, milliseconds);
    }

    public static String get(HttpRequestData data, String url, Map<String, String> input, String charset) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(url);
        boolean first = true;
        // reconstitute the query, ready for appends
        if (!url.contains("?"))
            urlBuilder.append("?");
        for (Entry<String, String> entry : input.entrySet()) {
            if (!first)
                urlBuilder.append('&');
            else
                first = false;
            urlBuilder.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), charset));
        }
        url = urlBuilder.toString();
        return get(data, url);
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url) throws IOException {
        return post(data, url, null);
    }

    public static String post(HttpRequestData data, String url, Map<String, String> inputs) throws IOException {
        return post(data, url, inputs, null);
    }

    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> inputs, Map<String, String> headers) throws IOException {
        url = urlSpecialChar(url);
        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(post, headers);
            if (null != inputs) {
                for (Entry<String, String> entry : inputs.entrySet()) {
                    post.addParameter(entry.getKey(), entry.getValue());
                }
            }
            return executeMethod(post, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }


    /**
     * http post调用
     */
    public static String post(HttpRequestData data, String url, Map<String, String> inputs, Map<String, String> headers, String encode) throws IOException {

        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(post, headers);
            httpClient.getParams().setParameter(
                    HttpMethodParams.HTTP_CONTENT_CHARSET, encode);
            if (null != inputs) {
                for (Entry<String, String> entry : inputs.entrySet()) {
                    post.addParameter(entry.getKey(), entry.getValue());
                }
            }
            return executeMethod(post, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * http post流
     * 用数据接口条用，添加编码
     */
    public static String post2(HttpRequestData data, String url, Map<String, String> inputs, String encode) throws IOException {
        StringBuilder postData = new StringBuilder();
        try {
            if (null != inputs) {
                for (Entry<String, String> entry : inputs.entrySet()) {
                    postData.append(URLEncoder.encode(entry.getKey(), encode)).append("=").append(URLEncoder.encode(entry.getValue(), encode)).append("&");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return postData(data, url, postData.toString(), encode);
    }

    /**
     * http post流
     * <p/>
     * 即不是post key-value模型的数据，而是post一段json文字
     */
    public static String postData(HttpRequestData data, String url, String param) throws IOException {
        return postData(data, url, param, DEFAOUT_CHARSET);
    }


    public static String postData(HttpRequestData data, String url, String param, String charset) throws IOException {
        return postData(data, url, param, charset, null);
    }

    public static String postData(HttpRequestData data, String url, String param, Map<String, String> headers) throws IOException {
        return postData(data, url, param, DEFAOUT_CHARSET, headers);
    }

    /**
     * http post流
     * <p/>
     * 即不是post key-value模型的数据，而是post一段json文字
     */
    public static String postData(HttpRequestData data, String url, String param, String charset, Map<String, String> headers) throws IOException {
        PostMethod post = new PostMethod(url);
        HttpClient httpClient = data.getHttpClient();
        try {
            initHttpMethod(post, headers);
            RequestEntity entity = new StringRequestEntity(param, "text/html", charset);
            post.setRequestEntity(entity);
            httpClient.executeMethod(post);
            return executeMethod(post, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    /**
     * 日志
     */
    private static void trace(HttpClient httpClient, String url, String html) {
        if (log.isInfoEnabled()) {
            log.info("===========================================" + url + "===========================================");
            for (Cookie e : httpClient.getState().getCookies()) {
                log.info(e.getName() + "===" + e.getValue() + ",threadId " + Thread.currentThread().getId());
            }
//			if (!StringUtils.isEmpty(html)) {
//				log.info(html);
//			} else {
//				log.info("html is empty");
//			}
            log.info("===========================================" + url + "===========================================");
        }
    }

    /**
     * protocol://host/href
     */
    private static String completeUrl(String base, String href) {
        return URLUtil.completeUrl(href, base);
    }

    public static String postFormData(HttpRequestData data, String url, Map<String, Object> inputs) throws IOException {
        return postFormData(data, url, inputs,null);
    }

    public static String postFormData(HttpRequestData data, String url, Map<String, Object> inputs, Map<String, String> headers) throws IOException {
        HttpClient httpClient = new HttpClient();
        PostMethod post = new PostMethod(url);
        try {
            initHttpMethod(post, headers);
            Part[] parts = new Part[inputs.size()];
            int index = 0;
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                Object valueObj = entry.getValue();
                if (valueObj instanceof byte[]) {
                    ByteArrayPartSource byteArrayPartSource = new ByteArrayPartSource("valid.jpeg", (byte[]) entry.getValue());
                    parts[index] = new FilePart(entry.getKey(), byteArrayPartSource, " image/jpeg", "utf-8");
                } else {
                    //其余为String
                    parts[index] = new StringPart(entry.getKey(), (String) entry.getValue(),"utf-8");
                }
                index ++;
            }
            MultipartRequestEntity entity = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(entity);
            return executeMethod(post, url, data);
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw e;
        } finally {
            post.releaseConnection();
            httpClient.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(FetchUtils.get(new HttpRequestData(), "http://www.baidu.com"));
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append(e.toString());
            StackTraceElement[] stes = e.getStackTrace();
            int length = Math.min(stes.length, 5);
            for (int i = 0; i < length; i++) {
                sb.append("\n\t").append("at ").append(stes[i].toString());
            }
            System.out.println(sb);
        }
    }

}

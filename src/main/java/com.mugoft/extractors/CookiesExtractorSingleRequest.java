package com.mugoft.extractors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mugoft.extractors.common.CookiesExtractor;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Class which allows to receive cookies from any website by sending single HTTP request to it
 */
public class CookiesExtractorSingleRequest extends CookiesExtractor {
    private final HttpClient client;
    private final String url;
    private List<Cookie> cookies = Arrays.asList();

    /**
     *
     * @param url url encoded url to the website, for which cookies should be received.
     * @throws UnsupportedEncodingException
     */
    public CookiesExtractorSingleRequest(String url) throws UnsupportedEncodingException {
        this.url = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        this.client = new HttpClient();
    }

    /**
     * Sends request to the specified URL and extracts cookies from the response
     *
     * @return status message to display for user
     */
    public void start() throws Exception {
        GetMethod method = new GetMethod(url);
        try{
            client.executeMethod(method);
            cookies = Arrays.asList(client.getState().getCookies());
            System.out.println("Response with cookies was successfully received");
        } catch(Exception e) {
            System.out.println("Error during sending request to " + url);
            throw e;
        } finally {
            method.releaseConnection();
        }
    }

    /**
     *
     * @return all received cookies
     */
    public String readCookies() {
        String cookiesJson = null;
        Gson gson = new GsonBuilder().create();
        cookiesJson = gson.toJson(cookies);
        return cookiesJson;
    }

    /**
     * Clears stored cookies
     */
    public void finish() {
        cookies = Arrays.asList();
        System.out.println("Cookies response is deleted");
    }
}

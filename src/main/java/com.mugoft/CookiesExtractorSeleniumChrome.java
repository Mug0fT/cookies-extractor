package com.mugoft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Class which allows to receive cookies from any website using selenium framework.
 */
public class CookiesExtractorSeleniumChrome {
    private WebDriver webDriver;
    private final String url;

    /**
     *
     * @param webDriverPath path to the selenium webdriver
     * @param url url encoded url to the website, for which cookies should be received.
     * @throws UnsupportedEncodingException
     */
    public CookiesExtractorSeleniumChrome(String webDriverPath, String url) throws UnsupportedEncodingException {
        this.url = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        System.setProperty("webdriver.chrome.driver", webDriverPath);

        webDriver = new ChromeDriver(ChromeDriverService.createDefaultService());
        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        webDriver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
    }

    /**
     * Opens chrome browser and navigates to {@link url} website.
     * After that user should, if necessary, perform some actions (e.g. accept cookies, solve captcha)
     * in order to get all necessary cookies for the website.
     */
    public void start() {
        webDriver.get(url);
    }

    /**
     *
     * @return all stored cookies from web browser for {@link url}
     */
    public String readCookies() {
        String cookiesJson = null;
        var cookies = webDriver.manage().getCookies();
        Gson gson = new GsonBuilder().create();
        cookiesJson = gson.toJson(cookies);
        return cookiesJson;
    }

    /**
     * Closes all opened by selenium chrome browsers
     */
    public void quit() {
        webDriver.quit();
    }
}

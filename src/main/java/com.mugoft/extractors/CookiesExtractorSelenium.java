package com.mugoft.extractors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mugoft.extractors.common.CookiesExtractor;
import com.mugoft.util.CookiesConverterHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Class which allows to receive cookies from any website using selenium framework.
 */
public class CookiesExtractorSelenium extends CookiesExtractor {


    private WebDriver webDriver;

    /**
     * Url to which the browser will navigate at beginning
     */
    private final String url;

    /**
     *
     * @param webDriverPath path to the selenium webdriver
     * @param url url encoded url to the website, for which cookies should be received.
     * @throws UnsupportedEncodingException
     */
    public CookiesExtractorSelenium(SeleniumBrowserType browserType, String webDriverPath, String url) throws UnsupportedEncodingException {
        this.url = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        System.setProperty(browserType.toString(), webDriverPath);

        switch (browserType) {
            case IE:
                webDriver = new InternetExplorerDriver();
                break;
            case EDGE:
                webDriver = new EdgeDriver();
                break;
            case CHROME:
                webDriver = new ChromeDriver(ChromeDriverService.createDefaultService());
                break;
            case OPERA:
                webDriver = new OperaDriver();
                break;
            case SAFARI:
                webDriver = new SafariDriver();
                break;
            case FIREFOX:
                webDriver = new FirefoxDriver();
                break;
        }

        webDriver.manage().window().maximize();
        webDriver.manage().timeouts().pageLoadTimeout(20, TimeUnit.SECONDS);
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        webDriver.manage().timeouts().setScriptTimeout(20, TimeUnit.SECONDS);
    }

    /*
     * @return true if browser is open, else returns false
     */
    public boolean isBrowserOpen() {
        try {
            webDriver.getTitle(); // driver.getCurrentUrl()
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Opens chrome browser and navigates to {@link url} website.
     * After that user should, if necessary, perform some actions (e.g. accept cookies, solve captcha)
     * in order to get all necessary cookies for the website. When finished - user should close
     *
     * @return status message to display for user. TODO: can be replaced by status codes if will be necessary
     */

    public void start() throws Exception {
        webDriver.get(url);
        System.out.println("Chrome browser is open. Please navigate in browser to the needed page, and accept all cookies.");
    }

    /**
     *
     * @return all stored cookies from web browser for {@link url}
     */
    public String readCookies() {
        String cookiesJson = null;
        var cookies = new LinkedList(webDriver.manage().getCookies());
        var cookiesApache = CookiesConverterHelper.SeleniumCookiesToApacheCookies(cookies);
        Gson gson = new GsonBuilder().create();
        cookiesJson = gson.toJson(cookiesApache);
        return cookiesJson;
    }

    /**
     * Closes all opened by selenium chrome browsers
     *
     * @return status message to display for user TODO: can be replaced by status codes if will be necessary
     */
    public void finish() {
        webDriver.quit();
        System.out.println("Chrome browser is closed.");
    }
}

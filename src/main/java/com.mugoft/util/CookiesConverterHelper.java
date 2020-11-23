package com.mugoft.util;

import org.apache.commons.httpclient.Cookie;

import java.util.LinkedList;
import java.util.List;


/**
 * Helps to convert different cookie formats from/into {@link Cookie} format.
 *
*/
public class CookiesConverterHelper {

    /**
     * Converts {@link org.openqa.selenium.Cookie} to {@link Cookie}
     *
     * @param cookiesSelenium cookies to convert
     * @return apache cookies ({@link Cookie})
     *  NOTE: as selenium cookie doesn't contain information about {@link Cookie#hasDomainAttribute} and
     *  {@link Cookie#hasPathAttribute}, these values are always set to false in returned cookies
     *
     */
    public static List<Cookie> SeleniumCookiesToApacheCookies(List<org.openqa.selenium.Cookie> cookiesSelenium) {
        List<Cookie> cookiesRet = new LinkedList<>();
        cookiesSelenium.stream().forEach(cookieSelenium -> {
            Cookie cookieRet = new Cookie();
            cookieRet.setName(cookieSelenium.getName());
            cookieRet.setDomain(cookieSelenium.getDomain());
            cookieRet.setPath(cookieSelenium.getPath());
            cookieRet.setValue(cookieSelenium.getValue());
            cookieRet.setExpiryDate(cookieSelenium.getExpiry());
            cookieRet.setSecure(cookieSelenium.isSecure());
            cookiesRet.add(cookieRet);
        });
        return cookiesRet;
    }

    public static List<org.openqa.selenium.Cookie> ApacheCookiesToSileniumCookies(List<Cookie> cookiesApache) {
        List<org.openqa.selenium.Cookie> cookiesRet = new LinkedList<>();
        cookiesApache.stream().forEach(cookieApache -> {
            org.openqa.selenium.Cookie cookieRet = new org.openqa.selenium.Cookie(
                    cookieApache.getName(),
                    cookieApache.getValue(),
                    cookieApache.getDomain(),
                    cookieApache.getPath(),
                    cookieApache.getExpiryDate(),
                    cookieApache.getSecure(),
                    false
            );
            cookiesRet.add(cookieRet);
        });
        return cookiesRet;
    }
}

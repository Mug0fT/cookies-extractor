package com.mugoft.extractors;

/**
 * @author mugoft
 * @created 22/11/2020 - 16:12
 * @project cookiesextractor
 */

/**
 * Enum which return browser type and associated system path variable for selenium driver
 */
public enum SeleniumBrowserType {
    CHROME("webdriver.chrome.driver"),
    EDGE("webdriver.edge.driver"),
    FIREFOX("webdriver.gecko.driver"),
    IE("webdriver.ie.driver"),
    OPERA(""),
    SAFARI("");

    private String value;

    SeleniumBrowserType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
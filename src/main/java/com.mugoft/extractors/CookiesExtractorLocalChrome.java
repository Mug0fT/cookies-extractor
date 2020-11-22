package com.mugoft.extractors;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mugoft.extractors.common.CookiesExtractor;
import org.apache.commons.httpclient.Cookie;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author mugoft
 * @created 22/11/2020 - 11:54
 * @project cookiesextractor
 */
public class CookiesExtractorLocalChrome extends CookiesExtractor {

    private final String hostKey;
    private final String pathToCookies;
    private Connection conn = null;


    public CookiesExtractorLocalChrome(String hostKey) throws UnsupportedEncodingException {
        this.hostKey = java.net.URLDecoder.decode(hostKey, StandardCharsets.UTF_8.name());
        pathToCookies = "";// TODO: detect auto
    }

    public CookiesExtractorLocalChrome(String hostKey, String pathToCookies) throws UnsupportedEncodingException {
        this.hostKey = java.net.URLDecoder.decode(hostKey, StandardCharsets.UTF_8.name());
        this.pathToCookies = pathToCookies;
    }

    /**
     * Sends request to the specified URL and extracts cookies from the response
     *
     * @return status message to display for user
     */
    public void start() throws Exception {
        try {
            // db parameters
            String url = "jdbc:sqlite:" + pathToCookies;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite chrome cookies has been established: " +  pathToCookies);

        } catch (SQLException e) {
            System.out.println("Error during establishing SQLite chrome cookies connection" +  pathToCookies);
            throw e;
        }
    }

    /**
     *
     * @return all received cookies
     */
    public String readCookies() {
        String sql = "SELECT * FROM cookies WHERE host_key LIKE \'%" + this.hostKey + "%\'";
        String cookiesJson = null;
        Gson gson = new GsonBuilder().create();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<Cookie> cookies = new LinkedList<>();
            // loop through the result set
            while (rs.next()) {
                Cookie cookie = new Cookie();
                cookie.setSecure(rs.getBoolean("is_secure"));
                cookie.setDomain(rs.getString("host_key"));
                cookie.setPath(rs.getString("path"));
                cookie.setName(rs.getString("name"));

                /**
                 * Google Chrome cookies DB has 2 columns for storing values: "value" and "encrypted_value",
                 * the last one is being used when the cookie stored was requested to be encrypted.
                 * Google Chrome uses triple DES encryption with the current users password as seed on windows machines
                 */
                String value = rs.getString("value");
                String valueEncrypted = rs.getString("encrypted_value");
                if(!Strings.isNullOrEmpty(value)) {
                    cookie.setValue(value);
                } else if (!Strings.isNullOrEmpty(valueEncrypted)) {
                    // TODO: decrypt using JDPAPI or similar
                    cookie.setValue(valueEncrypted);
                }

                String expiryDateStr = rs.getString("expires_utc");
                if(!Strings.isNullOrEmpty(expiryDateStr)) {
                    try {
                        Long expiryDateLong = Long.valueOf(expiryDateStr);
                        Instant instant = Instant.ofEpochMilli(expiryDateLong);
                        Date expiryDate = Date.from(instant);
                        cookie.setExpiryDate(expiryDate);

                    } catch (Exception e) {
                        System.out.println("Error during parsing expires_utc " + expiryDateStr + " for cookie " + cookie.getName());
                    }
                }
                cookies.add(cookie);
            }

            cookiesJson = gson.toJson(cookies);
        } catch (SQLException e) {
            System.out.println("Was not able to read cookies for hostKey " + this.hostKey);
        }
//        cookiesJson = gson.toJson(cookies);
        return cookiesJson;
    }

    /**
     * Clears stored cookies
     *
     * @return status message to display for user TODO: can be replaced by status codes if will be necessary
     */
    public void finish() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

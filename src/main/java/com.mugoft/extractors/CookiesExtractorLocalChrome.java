package com.mugoft.extractors;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mugoft.extractors.common.CookiesExtractor;
import com.mugoft.util.OsDetector;
import org.apache.commons.httpclient.Cookie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.sun.jna.platform.win32.Crypt32Util;

/**
 * @author mugoft
 * @created 22/11/2020 - 11:54
 * @project cookiesextractor
 */
public class CookiesExtractorLocalChrome extends CookiesExtractor {

    private final String hostKey;

    private final String pathToCookies;

    private Connection conn = null;

    private String chromeKeyringPassword = null;

    /**
     * Accesses the apple keyring to retrieve the Chrome decryption password
     * @param application
     * @return
     * @throws IOException
     */
    private static String getMacKeyringPassword(String application) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"security", "find-generic-password","-w", "-s", application};
        Process proc = rt.exec(commands);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String result = "";
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            result += s;
        }
        return result;
    }

    /**
     *
     * @param valueEncrypted
     * @return * @return decrypted value if success, valueEncrypted otherwise
     */
    private String decrypt(byte[] valueEncrypted) {
        byte[] decryptedBytes = null;
        if(OsDetector.getOS() == OsDetector.OS.WINDOWS){
            /**
             * Google Chrome uses triple DES encryption with the current users password as seed on windows machines
             */
            try {
                // For chrome under version 80
                // TODO: implement for chrome above 80, see https://stackoverflow.com/questions/60416350/chrome-80-how-to-decode-cookies
                decryptedBytes = Crypt32Util.cryptUnprotectData(valueEncrypted);
            } catch (Exception e){
                decryptedBytes = null;
            }
        } else if(OsDetector.getOS() == OsDetector.OS.LINUX){
            try {
                byte[] salt = "saltysalt".getBytes();
                char[] password = "peanuts".toCharArray();
                char[] iv = new char[16];
                Arrays.fill(iv, ' ');
                int keyLength = 16;

                int iterations = 1;

                PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
                SecretKeyFactory pbkdf2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

                byte[] aesKey = pbkdf2.generateSecret(spec).getEncoded();

                SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(new String(iv).getBytes()));

                // if cookies are encrypted "v10" is a the prefix (has to be removed before decryption)
                byte[] encryptedBytes = valueEncrypted;
                if (new String(valueEncrypted).startsWith("v10")) {
                    encryptedBytes = Arrays.copyOfRange(encryptedBytes, 3, encryptedBytes.length);
                }
                decryptedBytes = cipher.doFinal(encryptedBytes);
            } catch (Exception e) {
                decryptedBytes = null;
            }
        } else if(OsDetector.getOS() == OsDetector.OS.MAC){
            // access the decryption password from the keyring manager
            if(chromeKeyringPassword == null){
                try {
                    chromeKeyringPassword = getMacKeyringPassword("Chrome Safe Storage");
                } catch (IOException e) {
                    decryptedBytes = null;
                }
            }
            try {
                byte[] salt = "saltysalt".getBytes();
                char[] password = chromeKeyringPassword.toCharArray();
                char[] iv = new char[16];
                Arrays.fill(iv, ' ');
                int keyLength = 16;

                int iterations = 1003;

                PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength * 8);
                SecretKeyFactory pbkdf2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

                byte[] aesKey = pbkdf2.generateSecret(spec).getEncoded();

                SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(new String(iv).getBytes()));

                // if cookies are encrypted "v10" is a the prefix (has to be removed before decryption)
                byte[] encryptedBytes = valueEncrypted;
                if (new String(valueEncrypted).startsWith("v10")) {
                    encryptedBytes = Arrays.copyOfRange(encryptedBytes, 3, encryptedBytes.length);
                }
                decryptedBytes = cipher.doFinal(encryptedBytes);
            } catch (Exception e) {
                decryptedBytes = null;
            }
        }

        if(decryptedBytes == null){
            System.out.println("Was not able to decrypt encrypted value." );
            return new String(valueEncrypted);
        } else {
            return new String(decryptedBytes);
        }
    }

    public CookiesExtractorLocalChrome(String hostKey, String pathToCookies) throws UnsupportedEncodingException {
        this.hostKey = java.net.URLDecoder.decode(hostKey, StandardCharsets.UTF_8.name());
        this.pathToCookies = pathToCookies;
    }

    /**
     * Sends request to the specified URL and extracts cookies from the response
     */
    public void start() throws Exception {
        try {
            // db parameters
            String url = "jdbc:sqlite:" + pathToCookies;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite chrome cookies has been established: " +  pathToCookies);

        } catch (SQLException e) {
            System.out.println("Error during establishing SQLite chrome cookies connection: " +  pathToCookies);
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


                String value = rs.getString("value");
                /**
                 * Google Chrome cookies DB has 2 columns for storing values: "value" and "encrypted_value",
                 * the last one is being used when the cookie stored was requested to be encrypted.
                 */
                byte[] valueEncrypted = rs.getBytes("encrypted_value");
                if(!Strings.isNullOrEmpty(value)) {
                    cookie.setValue(value);
                } else if (valueEncrypted.length > 0) {
                    value = decrypt(valueEncrypted);
                    cookie.setValue(value);
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
        return cookiesJson;
    }

    /**
     * Clears stored cookies
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

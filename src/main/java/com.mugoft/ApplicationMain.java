package com.mugoft;

import com.mugoft.extractors.common.CookiesExtractor;
import com.mugoft.storageproviders.common.StorageProvider;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.beanutils.BeanDeclaration;
import org.apache.commons.configuration2.beanutils.BeanHelper;
import org.apache.commons.configuration2.beanutils.XMLBeanDeclaration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.util.List;
import java.util.Scanner;

/**
 * @author mugoft
 * @created 22/11/2020 - 19:36
 * @project cookiesextractor
 */

public class ApplicationMain {

    final static Scanner SCANNER = new Scanner(System.in);

    final static String CONFIG_PATH = "config/config.xml";

    static CookiesExtractor cookiesExtractor;

    static StorageProvider outputProvider;


    public static void main(String[] args) throws Exception {

        /**
         * Create {@link cookiesExtractor} and {@link outputProvider} based on the configuration file
         */
        try {
            loadConfigurations();
        } catch (Exception ex) {
            System.out.println("Configuration file couldn't be parsed. Make sure it exists and contains valid properties. " + CONFIG_PATH);
            throw ex;
        }


        /**
         * open webbrowser, wait until user performs actions to receive all necessary cookies for the specified website,
         * and get cookies in json format from the browser
         */
        String cookiesJson = null;
        try {
            cookiesExtractor.start();
            System.out.println("After finished - press any key to start reading and saving process.");
            SCANNER.nextLine();
            cookiesJson = cookiesExtractor.readCookies();
        } catch (Exception ex) {
            System.out.println("Error during starting and reading the cookies!");
            throw ex;
        } finally {
            cookiesExtractor.finish();
        }

        /**
         * store cookies if exist
         */
        if(cookiesJson == null) {
            System.out.println("No cookies are found!");
        } else {
            try {
                outputProvider.storeCookies(cookiesJson);
                System.out.println("Cookies are stored succesfully!");
            } catch (Exception ex) {
                System.out.println("Error during storing cookies!");
                throw ex;
            }
        }
    }

    private static void  loadConfigurations() throws ConfigurationException {

        Configurations configs = new Configurations();

            XMLConfiguration config = configs.xml(CONFIG_PATH);
            //NodeList list = config.getDocument().getElementsByTagName("website");
            List<String> websites = config.getList(String.class, "website[@name]");
            Integer websiteConfigIndex = selectWebsite(websites);

            BeanDeclaration beanCookiesGenerator = new XMLBeanDeclaration(config, "website("+websiteConfigIndex +").сookiesGenerator"); //@name='google'
            cookiesExtractor = (CookiesExtractor) BeanHelper.INSTANCE.createBean(beanCookiesGenerator);

            BeanDeclaration beanOutputProvider = new XMLBeanDeclaration(config, "website("+websiteConfigIndex +").outputProvider");
            outputProvider = (StorageProvider) BeanHelper.INSTANCE.createBean(beanOutputProvider);
    }

    private static Integer selectWebsite(List<String> websites) {

        System.out.println("Please select website to generate cookies for: ");

        for(int i = 0; i < websites.size(); i++) {
            System.out.println(i + ": " + websites.get(i));
        }

        try {
            Integer iWebsite = Integer.valueOf(SCANNER.nextLine());
            System.out.println(websites.get(iWebsite) + " has been selected");
            return iWebsite;

        } catch (Exception e) {
            System.out.println("Wrong value selected");
            return selectWebsite(websites);
        }
    }


}

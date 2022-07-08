package com.my.downloader.utils;

/**
 * @author hw
 */
public class MyUrlUtils {

    /**
     * add a parameter pari to the url.
     *
     * @param url   url
     * @param name  name
     * @param value value
     * @return the url appended with the parameter pari
     */
    public static String addParameterPariToUrl(String url, String name, String value) {
        if (null == url) {
            return null;
        }
        url = url.trim();
        if (url.contains("?")) {
            if (url.endsWith("&")) {
                // do nothing
            } else if (url.endsWith("?")) {
                // do nothing
            } else {
                url += "&";
            }
        } else {
            url += "?";

        }
        url += name + "=" + value;
        return url;
    }
}

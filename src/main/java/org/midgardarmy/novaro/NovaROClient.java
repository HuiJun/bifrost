package org.midgardarmy.novaro;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.midgardarmy.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NovaROClient {

    private static final Logger logger = LoggerFactory.getLogger(NovaROClient.class);

    static final String NOVARO_USER = ConfigUtils.get("novaro.user");
    static final String NOVARO_PASS = ConfigUtils.get("novaro.pass");

    protected static final String BASEURL = "https://www.novaragnarok.com/";
    static final String LOGIN = "module=account&action=login";

    protected static final String NO_RESULTS_MESSAGE = "No Results Found.";

    protected static CookieStore cookieStore = new BasicCookieStore();

    protected static boolean isCookieExpired() {
        if (cookieStore == null) {
            return true;
        }
        boolean expired = false;
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie == null || cookie.getExpiryDate() == null || cookie.getExpiryDate().before(new Date())) {
                expired = true;
            }
        }
        return expired;
    }

    protected static boolean hasLoginForm(Document xmlDocument) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String loginForm = "//div[@id=\"login\"]/form";
            Node node = (Node) xPath.compile(loginForm).evaluate(xmlDocument, XPathConstants.NODE);
            if (node != null && node.getNodeName().equals("form")) {
                return true;
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("hasLoginForm Error: ", e);
            }
        }

        return false;
    }

    protected static List<String> getDeepestValues(Node node) {
        List<String> results = new ArrayList<>();
        for (int k = 0; k < node.getChildNodes().getLength(); k++) {
            Node child = node.getChildNodes().item(k);
            if (child.getChildNodes().getLength() > 0) {
                results.addAll(getDeepestValues(child));
            } else {
                results.add(child.getNodeValue());
            }
        }

        return results;
    }

    protected static HttpResponse<String> getHTML(String url) throws UnirestException {
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());
        return Unirest.get(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .asString();
    }

    protected static synchronized void postLogin() throws UnirestException {
        String url = String.format("%s?%s", BASEURL, LOGIN);
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());
        Unirest.post(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("username", NOVARO_USER)
                .field("password", NOVARO_PASS)
                .field("server", "NovaRO")
                .asString();
    }

    public NovaROClient() {}
}

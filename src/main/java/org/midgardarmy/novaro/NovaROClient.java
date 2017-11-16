package org.midgardarmy.novaro;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.utils.ConfigUtils;

public class NovaROClient {

    private static final Logger logger = LoggerFactory.getLogger(NovaROClient.class);

    public static final String NOVARO_USER = ConfigUtils.get("novaro.user");
    public static final String NOVARO_PASS = ConfigUtils.get("novaro.pass");

    public static final String BASEURL = "https://www.novaragnarok.com/";

    public static final String LOGIN = "module=account&action=login";
    public static final String MARKET = "module=vending";
    public static final List<NameValuePair> SEARCH;
    static {
        SEARCH = new ArrayList<>();
        SEARCH.add(new BasicNameValuePair("module", "item"));
        SEARCH.add(new BasicNameValuePair("action", "index"));
        SEARCH.add(new BasicNameValuePair("type", "-1"));
    }
    public static final String ITEM = "module=item&action=index&id=";

    private static CookieStore cookieStore = new BasicCookieStore();

    public static synchronized List<EmbedObject> getByName(String name) {
        List<EmbedObject> resultList = new ArrayList<>();
        try {
            postLogin();
            URIBuilder b = new URIBuilder(BASEURL);
            b.setPath(BASEURL);
            List<NameValuePair> searchList = new ArrayList<>();
            searchList.addAll(SEARCH);
            searchList.add(new BasicNameValuePair("name", name));
            b.addParameters(searchList);
            logger.info(b.toString());
            HttpResponse<String> searchResult = getHTML(b.toString());

            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setDropEmptyParas(true);
            tidy.setMakeClean(true);
            tidy.setXmlTags(true);
            tidy.setXmlOut(true);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(searchResult.getBody().getBytes("UTF-8"));
            Document xmlDocument = tidy.parseDOM(inputStream, null);

            List<String> ids = extractIDs(xmlDocument);
            return getById(ids);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getByName Error: " + e.getLocalizedMessage());
            }
        }
        return resultList;
    }

    public static synchronized List<EmbedObject> getById(List<String> ids) {
        List<EmbedObject> resultList = new ArrayList<>();

        for (String id : ids) {

            try {
                URIBuilder b = new URIBuilder(BASEURL);
                b.setPath(BASEURL);
                b.addParameter("module", "item");
                b.addParameter("id", id);
                EmbedBuilder object = new EmbedBuilder();
                resultList.add(object.build());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getById: ", e);
                }
            }

        }

        if (resultList.isEmpty()) {
            EmbedObject object = new EmbedObject();
            object.description = "No Results Found";
            resultList.add(object);
        }

        return resultList;
    }

    private static List<String> extractIDs(Document xmlDocument) {
        logger.info(xmlDocument.toString());
        List<String> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//div[@id=\"mCSB_1_container\"]/div/table[2]/tbody/tr/td[1]/text()";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(nodes.item(i).getNodeValue());
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("extractIDs Error: " + e.getLocalizedMessage());
            }
        }

        return result;
    }

    private static HttpResponse<String> getHTML(String url) throws UnirestException {
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());
        return Unirest.get(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .header("Content-Type", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .queryString("module", LOGIN)
                .asString();
    }

    private static HttpResponse<String> postLogin() throws UnirestException {
        String url = String.format("%s?%s", BASEURL, LOGIN);
        logger.info("Login URL: " + url);
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());
        return Unirest.post(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .field("username", NOVARO_USER)
                .field("password", NOVARO_PASS)
                .field("server", "NovaRO")
                .asString();
    }

    private NovaROClient() {}

}

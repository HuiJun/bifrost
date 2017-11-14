package org.midgardarmy.novaro;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import org.midgardarmy.utils.ConfigUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class NovaROClient {

    private static final Logger logger = LoggerFactory.getLogger(NovaROClient.class);

    public static final String NOVARO_USER = ConfigUtils.get("novaro.user");
    public static final String NOVARO_PASS = ConfigUtils.get("novaro.pass");

    public static final String BASEURL = "https://www.novaragnarok.com/";

    public static final String LOGIN = "module=account&action=login";
    public static final String MARKET = "database/";

    public static CookieStore cookieStore = new BasicCookieStore();

    private static HttpResponse<String> getHTML(String url) throws UnirestException {
        return Unirest.get(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .header("Content-Type", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .queryString("module", LOGIN)
                .asString();
    }

    private static HttpResponse<String> postLogin(String url) throws UnirestException {
        Unirest.setHttpClient(org.apache.http.impl.client.HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build());
        return Unirest.get(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .header("Content-Type", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .queryString("module", LOGIN)
                .asString();
    }

    private NovaROClient() {}

}

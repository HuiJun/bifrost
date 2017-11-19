package org.midgardarmy.novaro;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.midgardarmy.utils.BotUtils;
import org.midgardarmy.utils.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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

    public static final List<NameValuePair> SEARCH;
    static {
        SEARCH = new ArrayList<>();
        SEARCH.add(new BasicNameValuePair("module", "item"));
        SEARCH.add(new BasicNameValuePair("action", "index"));
        SEARCH.add(new BasicNameValuePair("type", "-1"));
    }
    public static final List<NameValuePair> ITEM;
    static {
        ITEM = new ArrayList<>();
        ITEM.add(new BasicNameValuePair("module", "vending"));
        ITEM.add(new BasicNameValuePair("action", "item"));
    }

    private static CookieStore cookieStore = new BasicCookieStore();

    public static synchronized List<EmbedObject> getByName(String name) {
        List<EmbedObject> resultList = new ArrayList<>();
        try {
            if (cookieStore.getCookies().isEmpty()) {
                logger.info("Logging In");
                postLogin();
            }

            URIBuilder b = new URIBuilder(BASEURL);

            List<NameValuePair> searchList = new ArrayList<>();
            searchList.addAll(SEARCH);
            searchList.add(new BasicNameValuePair("name", name));
            b.addParameters(searchList);
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

            List<List<String>> items = extractIDs(xmlDocument);

            logger.info("Items Size: " + items.size());

            EmbedBuilder object = new EmbedBuilder();
            object.withTitle("Search Results");
            object.withDescription("```haskell");
            object.appendDescription(String.format("%n"));
            int itemCount = 0;
            String currentId = "";
            for (List<String> item : items) {
                if (item.size() == 4) {
                    StringBuilder sb = new StringBuilder();

                    sb.append(String.format("%sws %s", BotUtils.BOT_PREFIX, item.get(0)));
                    sb.append(String.join("", Collections.nCopies(12 - sb.length(), " ")));
                    sb.append(item.get(2));
                    sb.append(' ');
                    sb.append(String.format("(%s)", item.get(3)));
                    sb.append(String.format("%n"));

                    object.appendDescription(sb.toString());
                    itemCount++;
                    currentId = item.get(0);
                }
            }

            if (itemCount == 1) {
                return getById(Arrays.asList(currentId));
            }

            object.appendDescription(String.format("%n"));
            object.appendDescription("```");
            resultList.add(object.build());
            return resultList;

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getByName Error: ", e);
            }
        }
        return resultList;
    }

    public static synchronized List<EmbedObject> getById(List<String> ids) {
        List<EmbedObject> resultList = new ArrayList<>();

        List<Integer> intIds = new ArrayList<>();
        for (String id : ids) {
            intIds.add(Integer.parseInt(id));
        }
        Map<Integer, Map<String, Object>> items = DataUtils.getItemsByIDs(intIds);

        for (String id : ids) {
            try {
                if (cookieStore.getCookies().isEmpty()) {
                    logger.info("Logging in");
                    postLogin();
                }

                URIBuilder b = new URIBuilder(BASEURL);

                List<NameValuePair> itemList = new ArrayList<>();
                itemList.addAll(ITEM);
                itemList.add(new BasicNameValuePair("id", id));
                b.addParameters(itemList);
                HttpResponse<String> itemResult = getHTML(b.toString());

                Tidy tidy = new Tidy();
                tidy.setQuiet(true);
                tidy.setShowWarnings(false);
                tidy.setDropEmptyParas(true);
                tidy.setMakeClean(true);
                tidy.setXmlTags(true);
                tidy.setXmlOut(true);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(itemResult.getBody().getBytes("UTF-8"));
                Document xmlDocument = tidy.parseDOM(inputStream, null);

                List<List<String>> results = extractData(xmlDocument);

                EmbedBuilder object = new EmbedBuilder();
                object.withTitle(String.format("Vendors Selling %s", items.get(Integer.parseInt(id)).getOrDefault("name", "Unknown")));

                if (!results.isEmpty()) {
                    object.withDescription("```haskell");
                    object.appendDescription(String.format("%n"));

                    for (List<String> result : results) {
                        StringBuilder sb = new StringBuilder();
                        if (result.size() == 4) {
                            sb.append(String.format("%s", result.get(0)));
                            sb.append(String.join("", Collections.nCopies(15 - sb.length(), " ")));
                            sb.append(String.format("%s", result.get(1)));
                            sb.append(String.join("", Collections.nCopies(20 - sb.length(), " ")));
                            sb.append(StringUtils.abbreviate(result.get(2), 16));
                            sb.append(String.join("", Collections.nCopies(37 - sb.length(), " ")));
                            sb.append(String.format("%s", result.get(3)));
                            sb.append(String.format("%n"));
                        } else if (result.size() == 3) {
                            sb.append(String.format("%s", result.get(0)));
                            sb.append(String.join("", Collections.nCopies(14 - sb.length(), " ")));
                            sb.append(StringUtils.abbreviate(result.get(1), 16));
                            sb.append(String.join("", Collections.nCopies(37 - sb.length(), " ")));
                            sb.append(String.format("%s", result.get(2)));
                            sb.append(String.format("%n"));
                        } else {
                            sb.append("No Results Found");
                        }
                        object.appendDescription(sb.toString());

                    }
                    object.appendDescription(String.format("%n"));
                    object.appendDescription("```");
                    resultList.add(object.build());
                }
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

    private static List<List<String>> extractIDs(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'horizontal-table')]/tr";

            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                List<String> data = new ArrayList<>();
                for (int j = 0; j < nodes.item(i).getChildNodes().getLength(); j++) {
                    Node td = nodes.item(i).getChildNodes().item(j);
                    if (td.getNodeName().equals("td")) {
                        Node node = td.getChildNodes().item(0);
                        switch (node.getNodeName()) {
                            case "a":
                                data.add(j, node.getChildNodes().item(0).getNodeValue());
                                break;
                            case "img":
                                data.add(j, node.getAttributes().getNamedItem("src").getNodeValue());
                                break;
                            default:
                                data.add(j, node.getNodeValue());
                                break;
                        }
                    }
                }
                result.add(data);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("extractIDs Error: ", e);
            }
        }

        return result;
    }

    private static List<List<String>> extractData(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'horizontal-table')]/tbody/tr";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int h = 2; h < nodes.getLength(); h++) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < nodes.item(h).getChildNodes().getLength(); i++) {
                    if (nodes.item(h).getChildNodes().item(i).getNodeName().equals("td")) {
                        StringBuilder tableCell = new StringBuilder();
                        for (int j = 0; j < nodes.item(h).getChildNodes().item(i).getChildNodes().getLength(); j++) {
                            Node node = nodes.item(h).getChildNodes().item(i).getChildNodes().item(j);
                            switch (node.getNodeName()) {
                                case "span":
                                    String spanValue = node.getFirstChild().getNodeValue().trim();
                                    for (int k = 0; k < node.getAttributes().getLength(); k++) {
                                        Node attr = node.getAttributes().item(k);
                                        if (attr.getNodeName().equals("data-clipboard-text")) {
                                            spanValue = attr.getNodeValue().trim();
                                        }
                                    }
                                    tableCell.append(spanValue);
                                    break;

                                case "a":
                                    tableCell.append(node.getNodeValue().trim());
                                    break;

                                default:
                                    if (node.getChildNodes().getLength() > 0) {
                                        List<String> additional = new ArrayList<>();
                                        for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                            Node child = node.getChildNodes().item(k);
                                            if (child.getNodeName().equals("a")) {
                                                logger.info(child.getNodeValue());
                                                additional.add(child.getNodeValue().trim());
                                            }
                                        }
                                        tableCell.append(String.join(",", additional));
                                    } else {
                                        tableCell.append(node.getNodeValue().trim());
                                    }
                                    break;
                            }
                        }
                        data.add(tableCell.toString());
                    }
                }
                result.add(h - 2, data);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("extractIDs Error: ", e);
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
                .asString();
    }

    private static HttpResponse<String> postLogin() throws UnirestException {
        String url = String.format("%s?%s", BASEURL, LOGIN);
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

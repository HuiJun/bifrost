package org.midgardarmy.novaro.actions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.midgardarmy.novaro.NovaROClient;
import org.midgardarmy.utils.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class NovaROMarket extends NovaROClient {

    private static final Logger logger = LoggerFactory.getLogger(NovaROMarket.class);

    static final List<NameValuePair> SEARCH;
    static {
        SEARCH = new ArrayList<>();
        SEARCH.add(new BasicNameValuePair("module", "item"));
        SEARCH.add(new BasicNameValuePair("action", "index"));
        SEARCH.add(new BasicNameValuePair("type", "-1"));
    }

    private static final List<NameValuePair> ITEM;
    static {
        ITEM = new ArrayList<>();
        ITEM.add(new BasicNameValuePair("module", "vending"));
        ITEM.add(new BasicNameValuePair("action", "item"));
    }

    public static synchronized List<EmbedObject> getByName(Map<String, String> cache) {
        List<EmbedObject> resultList = new ArrayList<>();
        try {
            if (cookieStore.getCookies().isEmpty() || isCookieExpired()) {
                postLogin();
            }

            int refine = 0;

            String name = cache.get("itemName");
            int page = Integer.parseInt(cache.get("pageNum"));

            if (name.startsWith("+")) {
                int space = name.indexOf(' ');
                refine = Integer.parseInt(name.substring(1, space));
                name = name.substring(space + 1);
            }

            URIBuilder b = new URIBuilder(BASEURL);

            List<NameValuePair> searchList = new ArrayList<>(SEARCH);
            searchList.add(new BasicNameValuePair("name", name));
            if (page > 1) {
                searchList.add(new BasicNameValuePair("p", Integer.toString(page)));
            }
            b.addParameters(searchList);

            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setShowErrors(0);

            Document xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);
            if (hasLoginForm(xmlDocument)) {
                postLogin();
                xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);
            }
            List<List<String>> items = extractIDs(xmlDocument);
            int pageNum = getPages(xmlDocument);

            EmbedBuilder object = new EmbedBuilder();
            object.withColor(128, 0, 128);
            object.withTitle("Search Results");
            object.withDescription("```haskell");
            object.appendDescription(String.format("%n"));

            StringBuilder currentId = new StringBuilder();
            int itemCount = processSearchResults(object, "ws", items, currentId);
            if (itemCount == 1) {
                cache.put("itemName", currentId.toString());
                return getById(Arrays.asList(currentId.toString().split(" ")), 1, refine);
            } else if (itemCount == 0) {
                object.appendDescription(NO_RESULTS_MESSAGE);
            }

            object.appendDescription(String.format("%n"));
            object.appendDescription("```");

            addFooter(object, "ws", page, pageNum);

            resultList.add(object.build());
            return resultList;

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getByName Error: ", e);
            }
        }
        return resultList;
    }

    public static synchronized List<EmbedObject> getById(List<String> ids, int page, int refine) {
        List<EmbedObject> resultList = new ArrayList<>();

        for (String id : ids) {
            try {

                if (cookieStore.getCookies().isEmpty() || isCookieExpired()) {
                    postLogin();
                }

                URIBuilder b = new URIBuilder(BASEURL);

                List<NameValuePair> itemList = new ArrayList<>(ITEM);
                itemList.add(new BasicNameValuePair("id", id));

                if (page > 1) {
                    itemList.add(new BasicNameValuePair("p", Integer.toString(page)));
                }

                if (refine > 0) {
                    itemList.add(new BasicNameValuePair("refine_op", "gt"));
                    itemList.add(new BasicNameValuePair("refine", Integer.toString(refine)));
                }

                b.addParameters(itemList);

                Tidy tidy = new Tidy();
                tidy.setQuiet(true);
                tidy.setShowWarnings(false);
                tidy.setShowErrors(0);

                Document xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);

                if (hasLoginForm(xmlDocument)) {
                    postLogin();
                    xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);
                }

                List<List<String>> results = extractData(xmlDocument);
                String pageTitle = getItemTitle(xmlDocument);
                int pageNum = getPages(xmlDocument);

                EmbedBuilder object = new EmbedBuilder();
                object.withColor(128, 0, 128);
                object.withTitle(String.format("Vendors Selling %s", pageTitle));
                object.withDescription("```haskell");
                object.appendDescription(String.format("%n"));

                if (!results.isEmpty()) {
                    StringBuilder sbu = new StringBuilder();
                    for (List<String> result : results) {
                        StringBuilder sb = new StringBuilder();
                        switch (result.size()) {
                            case 3:
                                sb.append(result.get(0));
                                sb.append(String.join("", Collections.nCopies(14 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(1), 16));
                                sb.append(String.join("", Collections.nCopies(37 - sb.length(), " ")));
                                sb.append(result.get(2));
                                sb.append(String.format("%n"));
                                break;

                            case 4:
                                sb.append(result.get(0));
                                sb.append(String.join("", Collections.nCopies(15 - sb.length(), " ")));
                                sb.append(result.get(1));
                                sb.append(String.join("", Collections.nCopies(20 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(2), 16));
                                sb.append(String.join("", Collections.nCopies(37 - sb.length(), " ")));
                                sb.append(result.get(3));
                                sb.append(String.format("%n"));
                                break;

                            case 5:
                                sb.append(result.get(1));
                                sb.append(String.join("", Collections.nCopies(15 - sb.length(), " ")));
                                sb.append(result.get(2));
                                sb.append(String.join("", Collections.nCopies(20 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(3), 16));
                                sb.append(String.join("", Collections.nCopies(37 - sb.length(), " ")));
                                sb.append(result.get(4));
                                sb.append(String.format("%n"));
                                break;

                            default:
                                break;
                        }

                        sbu.append(sb.toString());
                    }

                    if (sbu.length() > 0) {
                        object.appendDescription(sbu.toString());
                    } else {
                        object.appendDescription(NO_RESULTS_MESSAGE);
                    }

                } else {
                    object.appendDescription(NO_RESULTS_MESSAGE);
                }

                object.appendDescription(String.format("%n"));
                object.appendDescription("```");
                addFooter(object, "ws", page, pageNum);

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

    static List<List<String>> extractIDs(Document xmlDocument) {
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

    static List<List<String>> extractData(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'horizontal-table')]/tbody/tr";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int h = 0; h < nodes.getLength(); h++) {
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
                                    tableCell.append(node.getFirstChild().getNodeValue().trim());
                                    break;

                                default:
                                    if (node.getChildNodes().getLength() > 0) {
                                        List<String> additional = new ArrayList<>();
                                        for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                            Node child = node.getChildNodes().item(k);
                                            if (child.getNodeName().equals("a")) {
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
                result.add(h, data);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("extractData Error: ", e);
            }
        }

        return result;
    }

    static String getItemTitle(Document xmlDocument) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String title = "//span[contains(@class, 'tooltip')]/a";

            Node node = (Node) xPath.compile(title).evaluate(xmlDocument, XPathConstants.NODE);
            if (node != null && node.getNodeName().equals("a")) {
                return node.getFirstChild().getNodeValue();
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getItemTitle Error: ", e);
            }
        }

        return "Unknown";
    }

    static int getPages(Document xmlDocument) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String pageNums = "//div[contains(@class, 'pages')]/a[contains(@class, 'page-num')]";
            String pageNext = "//div[contains(@class, 'pages')]/a[contains(@class, 'page-next')]";

            NodeList nodes = (NodeList) xPath.compile(pageNums).evaluate(xmlDocument, XPathConstants.NODESET);
            Node node = (Node) xPath.compile(pageNext).evaluate(xmlDocument, XPathConstants.NODE);
            if (node != null && node.getNodeName().equals("a")) {
                return 11;
            }
            return nodes.getLength();
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getPages Error: ", e);
            }
        }

        return 1;
    }

    static int processSearchResults(EmbedBuilder object, String command, List<List<String>> items, StringBuilder currentId) {
        int count = 0;
        for (List<String> item : items) {
            if (item.size() == 4) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s%s %s", BotUtils.BOT_PREFIX, command, item.get(0)));
                sb.append(String.join("", Collections.nCopies(12 - sb.length(), " ")));
                sb.append(item.get(2));
                sb.append(' ');
                sb.append(String.format("(%s)", item.get(3)));

                object.appendDescription(String.format("%s%n", StringUtils.abbreviate(sb.toString(), 60)));
                count++;
                currentId.append(item.get(0));
            }
        }
        return count;
    }

    static void addFooter(EmbedBuilder object, String command, int page, int pageNum) {
        if (pageNum > 10) {
            object.withFooterText(String.format("Page %1$d of %2$s (Use %3$s%4$s next, %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page > 1 ? page : 1, "10+", BotUtils.BOT_PREFIX, command));
        } else {
            if (pageNum > 1) {
                if (page == 1) {
                    object.withFooterText(String.format("Page 1 of %1$d (Use %2$s%3$s next or %2$s%3$s page [page number] to navigate)", pageNum, BotUtils.BOT_PREFIX, command));
                } else if (page < pageNum) {
                    object.withFooterText(String.format("Page %1$d of %2$d (Use %3$s%4$s next, %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page > 1 ? page : 1, pageNum, BotUtils.BOT_PREFIX, command));
                } else if (page == pageNum) {
                    object.withFooterText(String.format("Page %1$d of %2$d (Use %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page, pageNum, BotUtils.BOT_PREFIX, command));
                }
            } else {
                if (pageNum == 0 && page > 1) {
                    object.withFooterText("Well, that sucked. You probably did something wrong.");
                } else {
                    object.withFooterText("Page 1 of 1");
                }
            }
        }
    }
}

package net.zerofill.novaro.actions;

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

import net.zerofill.utils.SortUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import net.zerofill.novaro.NovaROClient;
import net.zerofill.utils.BotUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

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

    public static synchronized List<String> getByName(Map<String, String> cache) {
        List<String> resultList = new ArrayList<>();
        try {
            if (cookieStore.getCookies().isEmpty() || isCookieExpired()) {
                postLogin();
            }

            String name = cache.get("itemName");
            int refine = Integer.parseInt(cache.get("refine"));
            int page = Integer.parseInt(cache.get("pageNum"));

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

            StringBuilder object = new StringBuilder();
            object.append("Search Results");
            object.append(String.format("%n"));

            object.append("```haskell");
            object.append(String.format("%n"));

            StringBuilder currentId = new StringBuilder();
            int itemCount = processSearchResults(object, "ws", items, currentId);
            if (itemCount == 1) {
                cache.put("itemName", currentId.toString());
                return getById(Arrays.asList(currentId.toString().split(" ")), 1, refine);
            } else if (itemCount == 0) {
                object.append(NO_RESULTS_MESSAGE);
            }

            object.append(String.format("%n"));
            object.append("```");

            addFooter(object, "ws", page, pageNum);

            resultList.add(object.toString());
            return resultList;

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getByName Error: ", e);
            }
        }
        return resultList;
    }

    public static synchronized List<String> getById(List<String> ids, int page, int refine) {
        List<String> resultList = new ArrayList<>();

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

                if (refine > 0) {
                    results = filterByRefine(results, refine, 1);
                }

                String pageTitle = getItemTitle(xmlDocument);
                int pageNum = getPages(xmlDocument);

                StringBuilder object = new StringBuilder();
                object.append(String.format("Vendors Selling %s", pageTitle));
                object.append(String.format("%n"));

                object.append("```haskell");
                object.append(String.format("%n"));

                if (!results.isEmpty()) {
                    StringBuilder sbu = new StringBuilder();
                    SortUtil cc = new SortUtil(0);
                    Collections.sort(results, cc);
                    for (List<String> result : results) {
                        StringBuilder sb = new StringBuilder();
                        switch (result.size()) {
                            case 3:
                                sb.append(result.get(0));
                                sb.append(String.join("", Collections.nCopies(14 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(1), 36));
                                sb.append(String.join("", Collections.nCopies(57 - sb.length(), " ")));
                                sb.append(result.get(2));
                                sb.append(String.format("%n"));
                                break;

                            case 4:
                                sb.append(result.get(0));
                                sb.append(String.join("", Collections.nCopies(15 - sb.length(), " ")));
                                sb.append(result.get(1));
                                sb.append(String.join("", Collections.nCopies(20 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(2), 36));
                                sb.append(String.join("", Collections.nCopies(57 - sb.length(), " ")));
                                sb.append(result.get(3));
                                sb.append(String.format("%n"));
                                break;

                            case 5:
                                sb.append(result.get(1));
                                sb.append(String.join("", Collections.nCopies(15 - sb.length(), " ")));
                                sb.append(result.get(2));
                                sb.append(String.join("", Collections.nCopies(20 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(3), 36));
                                sb.append(String.join("", Collections.nCopies(57 - sb.length(), " ")));
                                sb.append(result.get(4));
                                sb.append(String.format("%n"));
                                break;

                            default:
                                break;
                        }

                        if (sbu.length() + sb.length() < 1900) {
                            sbu.append(sb.toString());
                        }
                    }

                    if (sbu.length() > 0) {
                        object.append(sbu.toString());
                    } else {
                        object.append(NO_RESULTS_MESSAGE);
                    }

                } else {
                    object.append(NO_RESULTS_MESSAGE);
                }

                object.append(String.format("%n"));
                object.append("```");
                addFooter(object, "ws", page, pageNum);

                resultList.add(object.toString());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getById: ", e);
                }
            }
        }

        if (resultList.isEmpty()) {
            StringBuilder object = new StringBuilder();
            object.append(String.format("%nNo Results Found"));
            resultList.add(object.toString());
        }

        return resultList;
    }

    static List<List<String>> extractIDs(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'nova-table')]/tr";

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
            String expression = "//table[contains(@class, 'nova-table')]/tbody/tr";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int h = 0; h < nodes.getLength(); h++) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < nodes.item(h).getChildNodes().getLength(); i++) {
                    if (nodes.item(h).getChildNodes().item(i).getNodeName().equals("td")) {
                        StringBuilder tableCell = new StringBuilder();
                        for (int j = 0; j < nodes.item(h).getChildNodes().item(i).getChildNodes().getLength(); j++) {
                            Node node = nodes.item(h).getChildNodes().item(i).getChildNodes().item(j);
                            switch (node.getNodeName()) {

                                case "div":
                                    String childSpanValue = node.getFirstChild().getFirstChild().getNodeValue().trim();
                                    for (int k = 0; k < node.getFirstChild().getAttributes().getLength(); k++) {
                                        Node attr = node.getFirstChild().getAttributes().item(k);
                                        if (attr.getNodeName().equals("data-clipboard-text")) {
                                            childSpanValue = attr.getNodeValue().trim();
                                        }
                                    }
                                    tableCell.append(childSpanValue);
                                    break;

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

    static int processSearchResults(StringBuilder object, String command, List<List<String>> items, StringBuilder currentId) {
        int count = 0;
        for (List<String> item : items) {
            if (item.size() == 4) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%s%s %s", BotUtils.BOT_PREFIX, command, item.get(0)));
                sb.append(String.join("", Collections.nCopies(12 - sb.length(), " ")));
                sb.append(item.get(2));
                sb.append(' ');
                sb.append(String.format("(%s)", item.get(3)));

                object.append(String.format("%n%s", StringUtils.abbreviate(sb.toString(), 60)));
                count++;
                currentId.append(item.get(0));
            }
        }
        return count;
    }

    static List<List<String>> filterByRefine(List<List<String>> items, int refine, int col) {
        List<List<String>> filtered = new ArrayList<>();
        for (List<String> item : items) {
            if (item.size() > col + 1 && item.get(col).startsWith("+") && Integer.parseInt(item.get(col).substring(1)) >= refine) {
                logger.debug("Removing item");
                filtered.add(item);
            }
        }
        return filtered;
    }

    static void addFooter(StringBuilder object, String command, int page, int pageNum) {
        object.append(String.format("%n"));
        object.append("```haskell");
        object.append(String.format("%n"));

        if (pageNum > 10) {
            object.append(String.format("Page %1$d of %2$s (Use %3$s%4$s next, %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page > 1 ? page : 1, "10+", BotUtils.BOT_PREFIX, command));
        } else {
            if (pageNum > 1) {
                if (page == 1) {
                    object.append(String.format("Page 1 of %1$d (Use %2$s%3$s next or %2$s%3$s page [page number] to navigate)", pageNum, BotUtils.BOT_PREFIX, command));
                } else if (page < pageNum) {
                    object.append(String.format("Page %1$d of %2$d (Use %3$s%4$s next, %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page > 1 ? page : 1, pageNum, BotUtils.BOT_PREFIX, command));
                } else if (page == pageNum) {
                    object.append(String.format("Page %1$d of %2$d (Use %3$s%4$s prev or %3$s%4$s page [page number] to navigate)", page, pageNum, BotUtils.BOT_PREFIX, command));
                }
            } else {
                if (pageNum == 0 && page > 1) {
                    object.append("Well, that sucked. You probably did something wrong.");
                } else {
                    object.append("Page 1 of 1");
                }
            }
        }
        object.append("```");
    }
}

package net.zerofill.novaro.actions;

import com.mashape.unirest.http.HttpResponse;
import net.zerofill.novaro.NovaROClient;
import net.zerofill.novaro.actions.templates.BaseParser;
import net.zerofill.novaro.actions.templates.GuildRankingParser;
import net.zerofill.novaro.actions.templates.ZenyRankingParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NovaRORanking extends NovaROClient {

    private static final Logger logger = LoggerFactory.getLogger(NovaRORanking.class);

    private static final List<NameValuePair> ZENYRANKING;
    private static final List<NameValuePair> GUILDRANKING;

    static {
        ZENYRANKING = new ArrayList<>();
        ZENYRANKING.add(0, new BasicNameValuePair("module", "ranking"));
        ZENYRANKING.add(1, new BasicNameValuePair("action", "zeny"));

        GUILDRANKING = new ArrayList<>();
        GUILDRANKING.add(0, new BasicNameValuePair("module", "ranking"));
        GUILDRANKING.add(1, new BasicNameValuePair("action", "guild"));
    }

    public static synchronized List<String> getZenyRanking() {
        return getRanking(ZENYRANKING, new ZenyRankingParser());
    }

    public static synchronized List<String> getGuildRanking() {
        return getRanking(GUILDRANKING, new GuildRankingParser());
    }

    public static synchronized List<String> getRanking(List<NameValuePair> nvp, BaseParser parser) {
        List<String> resultList = new ArrayList<>();

        try {
            if (cookieStore.getCookies().isEmpty() || isCookieExpired()) {
                postLogin();
            }

            URIBuilder b = new URIBuilder(BASEURL);

            List<NameValuePair> zenyRank = new ArrayList<>(nvp);
            b.addParameters(zenyRank);
            HttpResponse<String> zenyResult = getHTML(b.toString());

            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setShowErrors(0);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(zenyResult.getBody().getBytes(StandardCharsets.UTF_8));
            Document xmlDocument = tidy.parseDOM(inputStream, null);

            if (hasLoginForm(xmlDocument)) {
                postLogin();
                xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);
            }

            List<List<String>> results = extractData(xmlDocument);

            String action = Character.toUpperCase(nvp.get(1).getValue().charAt(0)) + nvp.get(1).getValue().substring(1);

            if (!results.isEmpty()) {

                String mhu = parser.execute(results);

                logger.debug(String.format("MHU length: %d", mhu.length()));

                if (mhu.length() > 0) {
                    StringBuilder mhBuilder = new StringBuilder();
                    mhBuilder.append(action);
                    mhBuilder.append(" Rankings");

                    mhBuilder.append("```haskell");
                    mhBuilder.append(String.format("%n"));
                    mhBuilder.append(mhu);
                    mhBuilder.append("```");
                    resultList.add(0, mhBuilder.toString());
                }

            } else {
                StringBuilder object = new StringBuilder();

                object.append(action);
                object.append(" Rankings");

                object.append("```");
                object.append(String.join("", Collections.nCopies(110, "-")));
                object.append("```");

                object.append("```haskell");
                object.append(String.format("%n"));
                object.append(NO_RESULTS_MESSAGE);
                object.append(String.format("%n"));
                object.append("```");

                resultList.add(object.toString());
            }

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getZenyRanking: ", e);
            }
        }

        return resultList;
    }

    static List<List<String>> extractData(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'horizontal-table')]/tr";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int h = 0; h < nodes.getLength(); h++) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < nodes.item(h).getChildNodes().getLength(); i++) {
                    if (nodes.item(h).getChildNodes().item(i).getNodeName().equals("td")) {
                        StringBuilder tableCell = new StringBuilder();
                        for (int j = 0; j < nodes.item(h).getChildNodes().item(i).getChildNodes().getLength(); j++) {
                            Node node = nodes.item(h).getChildNodes().item(i).getChildNodes().item(j);
                            if (node.getChildNodes().getLength() > 0) {
                                List<String> additional = new ArrayList<>();
                                for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                    Node child = node.getChildNodes().item(k);
                                    additional.add(child.getNodeValue().trim());
                                }
                                tableCell.append(String.join(",", additional));
                            } else {
                                tableCell.append(node.getNodeValue().trim());
                            }
                        }
                        data.add(tableCell.toString());
                    }
                }
                result.add(h, data);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("extractIDs Error: ", e);
            }
        }

        return result;
    }
}

package org.midgardarmy.novaro.actions;

import com.mashape.unirest.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.midgardarmy.novaro.NovaROClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

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

    static {
        ZENYRANKING = new ArrayList<>();
        ZENYRANKING.add(new BasicNameValuePair("module", "ranking"));
        ZENYRANKING.add(new BasicNameValuePair("action", "zeny"));
    }

    public static synchronized List<EmbedObject> getZenyRanking() {
        List<EmbedObject> resultList = new ArrayList<>();

        try {
            if (cookieStore.getCookies().isEmpty() || isCookieExpired()) {
                postLogin();
            }

            URIBuilder b = new URIBuilder(BASEURL);

            List<NameValuePair> zenyRank = new ArrayList<>(ZENYRANKING);
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

            if (!results.isEmpty()) {

                StringBuilder mhu = new StringBuilder();
                boolean mhh = false;
                for (List<String> result : results) {
                    switch (result.size()) {
                        case 7:
                        case 8:
                            StringBuilder mh = new StringBuilder();

                            int padding = 0;
                            if (!mhh) {
                                mh.append("Rank");
                                mh.append(String.join("", Collections.nCopies(6 - mh.length(), " ")));
                                mh.append("Name");
                                mh.append(String.join("", Collections.nCopies(30 - mh.length(), " ")));
                                mh.append("Zeny");
                                mh.append(String.join("", Collections.nCopies(43 - mh.length(), " ")));
                                //mh.append("Class");
                                //mh.append(String.join("", Collections.nCopies(40 - mh.length(), " ")));
                                //mh.append("Guild");
                                //mh.append(String.join("", Collections.nCopies(55 - mh.length(), " ")));
                                mh.append(String.format("%n"));

                                padding = mh.length();
                                mhh = true;
                            }

                            mh.append(result.get(0));
                            mh.append(String.join("", Collections.nCopies(6 - mh.length() + padding, " ")));
                            mh.append(result.get(1));
                            mh.append(String.join("", Collections.nCopies(30 - mh.length() + padding, " ")));
                            mh.append(result.get(2));
                            mh.append(String.join("", Collections.nCopies(43 - mh.length() + padding, " ")));
                            //mh.append(result.get(3));
                            //mh.append(String.join("", Collections.nCopies(49 - mh.length() + padding, " ")));
                            //mh.append(result.get(7));
                            //mh.append(String.join("", Collections.nCopies(67 - mh.length() + padding, " ")));
                            mh.append(String.format("%n"));

                            mhu.append(mh.toString());
                            break;

                        default:
                            break;
                    }
                }

                logger.debug("MHU length: " + mhu.length());

                if (mhu.length() > 0) {
                    EmbedBuilder mhBuilder = new EmbedBuilder();
                    mhBuilder.withColor(128, 0, 128);
                    mhBuilder.withTitle("Zeny Rankings");
                    mhBuilder.withDescription("```haskell");
                    mhBuilder.appendDescription(String.format("%n"));
                    mhBuilder.appendDescription(mhu.toString());
                    mhBuilder.appendDescription("```");
                    resultList.add(0, mhBuilder.build());
                }

            } else {
                EmbedBuilder object = new EmbedBuilder();
                object.withColor(128, 0, 128);
                object.withTitle("Zeny Rankings");

                object.withDescription("```");
                object.appendDescription(String.join("", Collections.nCopies(110, "-")));
                object.appendDescription("```");

                object.appendDescription("```haskell");
                object.appendDescription(String.format("%n"));
                object.appendDescription(NO_RESULTS_MESSAGE);
                object.appendDescription(String.format("%n"));
                object.appendDescription("```");

                resultList.add(object.build());
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

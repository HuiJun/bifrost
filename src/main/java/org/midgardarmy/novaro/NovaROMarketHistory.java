package org.midgardarmy.novaro;

import com.mashape.unirest.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NovaROMarketHistory extends NovaROMarket {

    private static final Logger logger = LoggerFactory.getLogger(NovaROMarketHistory.class);

    private static final List<NameValuePair> ITEMHISTORY;
    static {
        ITEMHISTORY = new ArrayList<>();
        ITEMHISTORY.add(new BasicNameValuePair("module", "vending"));
        ITEMHISTORY.add(new BasicNameValuePair("action", "itemhistory"));
    }

    private static CookieStore cookieStore = new BasicCookieStore();

    public static synchronized List<EmbedObject> getByName(Map<String, String> cache) {
        List<EmbedObject> resultList = new ArrayList<>();
        try {
            if (cookieStore.getCookies().isEmpty()) {
                NovaROMarket.postLogin();
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

            List<NameValuePair> searchList = new ArrayList<>();
            searchList.addAll(SEARCH);
            searchList.add(new BasicNameValuePair("name", name));
            if (page > 1) {
                searchList.add(new BasicNameValuePair("p", Integer.toString(page)));
            }
            b.addParameters(searchList);
            HttpResponse<String> searchResult = NovaROMarket.getHTML(b.toString());

            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setShowErrors(0);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(searchResult.getBody().getBytes("UTF-8"));
            Document xmlDocument = tidy.parseDOM(inputStream, null);

            List<List<String>> items = NovaROMarket.extractIDs(xmlDocument);
            int pageNum = NovaROMarket.getPages(xmlDocument);

            EmbedBuilder object = new EmbedBuilder();
            object.withColor(128, 0, 128);
            object.withTitle("Search Results");
            object.withDescription("```haskell");
            object.appendDescription(String.format("%n"));

            StringBuilder currentId = new StringBuilder();
            int itemCount = NovaROMarket.processSearchResults(object, "pc", items, currentId);

            if (itemCount == 1) {
                cache.put("itemName", currentId.toString());
                return getById(Arrays.asList(currentId.toString().split(" ")), 0, refine);
            } else if (itemCount == 0) {
                object.appendDescription(NO_RESULTS_MESSAGE);
            }

            object.appendDescription(String.format("%n"));
            object.appendDescription("```");

            NovaROMarket.addFooter(object, "pc", page, pageNum);

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
                if (cookieStore.getCookies().isEmpty()) {
                    NovaROMarket.postLogin();
                }

                URIBuilder b = new URIBuilder(BASEURL);

                List<NameValuePair> itemList = new ArrayList<>();
                itemList.addAll(ITEMHISTORY);
                itemList.add(new BasicNameValuePair("id", id));
                if (page > 1) {
                    itemList.add(new BasicNameValuePair("p", Integer.toString(page)));
                }

                if (refine > 0) {
                    itemList.add(new BasicNameValuePair("refine_op", "gt"));
                    itemList.add(new BasicNameValuePair("refine", Integer.toString(refine)));
                }
                b.addParameters(itemList);
                HttpResponse<String> itemResult = NovaROMarket.getHTML(b.toString());

                Tidy tidy = new Tidy();
                tidy.setQuiet(true);
                tidy.setShowWarnings(false);
                tidy.setShowErrors(0);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(itemResult.getBody().getBytes("UTF-8"));
                Document xmlDocument = tidy.parseDOM(inputStream, null);

                List<List<String>> results = NovaROMarket.extractData(xmlDocument);

                String pageTitle = getItemTitle(xmlDocument);
                int pageNum = getPages(xmlDocument);

                EmbedBuilder object = new EmbedBuilder();
                object.withColor(128, 0, 128);
                object.withTitle(String.format("Transaction History for %s", pageTitle));
                object.withDescription("```haskell");
                object.appendDescription(String.format("%n"));

                if (!results.isEmpty()) {

                    StringBuilder sbu = new StringBuilder();
                    StringBuilder mhu = new StringBuilder();
                    boolean mhh = false;
                    for (List<String> result : results) {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder mh = new StringBuilder();
                        logger.debug("" + result.size());
                        switch (result.size()) {
                            case 3:
                                sb.append(String.format("%s", result.get(0)).substring(0, 8));
                                sb.append(String.join("", Collections.nCopies(12 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(1), 16));
                                sb.append(String.join("", Collections.nCopies(27 - sb.length(), " ")));
                                sb.append(result.get(2));
                                sb.append(String.format("%n"));
                                break;

                            case 4:
                                sb.append(String.format("%s", result.get(0)).substring(0, 8));
                                sb.append(String.join("", Collections.nCopies(9 - sb.length(), " ")));
                                sb.append(result.get(1));
                                sb.append(String.join("", Collections.nCopies(24 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(2), 4));
                                sb.append(String.join("", Collections.nCopies(28 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(3), 27));
                                sb.append(String.format("%n"));
                                break;

                            case 6:
                                int padding = 0;
                                if (!mhh) {
                                    mh.append("");
                                    mh.append(String.join("", Collections.nCopies(6 - mh.length(), " ")));
                                    mh.append("");
                                    mh.append(String.join("", Collections.nCopies(12 - mh.length(), " ")));
                                    mh.append("Min");
                                    mh.append(String.join("", Collections.nCopies(25 - mh.length(), " ")));
                                    mh.append("Max");
                                    mh.append(String.join("", Collections.nCopies(40 - mh.length(), " ")));
                                    mh.append("Average");
                                    mh.append(String.join("", Collections.nCopies(55 - mh.length(), " ")));
                                    mh.append(String.format("%n"));
                                    mhh = true;
                                    padding = mh.length();
                                }
                                mh.append(result.get(0));
                                mh.append(String.join("", Collections.nCopies(6 - mh.length() + padding, " ")));
                                mh.append(result.get(1));
                                mh.append(String.join("", Collections.nCopies(12 - mh.length() + padding, " ")));
                                mh.append(result.get(2));
                                mh.append(String.join("", Collections.nCopies(25 - mh.length() + padding, " ")));
                                mh.append(result.get(3));
                                mh.append(String.join("", Collections.nCopies(40 - mh.length() + padding, " ")));
                                mh.append(result.get(4));
                                mh.append(String.join("", Collections.nCopies(55 - mh.length() + padding, " ")));
                                mh.append(String.format("%n"));
                                break;

                            default:
                                break;
                        }

                        sbu.append(sb.toString());
                        mhu.append(mh.toString());
                    }

                    if (mhu.length() > 0) {
                        EmbedBuilder mhBuilder = new EmbedBuilder();
                        mhBuilder.withColor(128, 0, 128);
                        mhBuilder.withTitle(String.format("Market History for %s", pageTitle));
                        mhBuilder.withDescription("```haskell");
                        mhBuilder.appendDescription(String.format("%n"));
                        mhBuilder.appendDescription(mhu.toString());
                        mhBuilder.appendDescription("```");
                        resultList.add(0, mhBuilder.build());
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
                addFooter(object, "pc", page, pageNum);

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

}

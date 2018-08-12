package net.zerofill.novaro.actions;

import com.mashape.unirest.http.HttpResponse;
import net.zerofill.utils.SortUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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

            ByteArrayInputStream inputStream = new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8));
            Document xmlDocument = tidy.parseDOM(inputStream, null);

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
            int itemCount = processSearchResults(object, "pc", items, currentId);

            if (itemCount == 1) {
                cache.put("itemName", currentId.toString());
                return getById(Arrays.asList(currentId.toString().split(" ")), 0, refine);
            } else if (itemCount == 0) {
                object.append(NO_RESULTS_MESSAGE);
            }

            object.append(String.format("%n"));
            object.append("```");

            addFooter(object, "pc", page, pageNum);

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

                List<NameValuePair> itemList = new ArrayList<>(ITEMHISTORY);
                itemList.add(new BasicNameValuePair("id", id));
                if (page > 1) {
                    itemList.add(new BasicNameValuePair("p", Integer.toString(page)));
                }

                if (refine > 0) {
                    itemList.add(new BasicNameValuePair("refine_op", "gt"));
                    itemList.add(new BasicNameValuePair("refine", Integer.toString(refine)));
                }
                b.addParameters(itemList);
                HttpResponse<String> itemResult = getHTML(b.toString());

                Tidy tidy = new Tidy();
                tidy.setQuiet(true);
                tidy.setShowWarnings(false);
                tidy.setShowErrors(0);

                ByteArrayInputStream inputStream = new ByteArrayInputStream(itemResult.getBody().getBytes(StandardCharsets.UTF_8));
                Document xmlDocument = tidy.parseDOM(inputStream, null);

                if (hasLoginForm(xmlDocument)) {
                    postLogin();
                    xmlDocument = tidy.parseDOM(new ByteArrayInputStream(getHTML(b.toString()).getBody().getBytes(StandardCharsets.UTF_8)), null);
                }

                List<List<String>> results = extractData(xmlDocument);

                if (refine > 0) {
                    results = filterByRefine(results, refine, 2);
                }

                String pageTitle = getItemTitle(xmlDocument);
                int pageNum = getPages(xmlDocument);

                StringBuilder object = new StringBuilder();
                object.append(String.format("Transaction History for %s", pageTitle));
                object.append(String.format("%n"));

                object.append("```haskell");
                object.append(String.format("%n"));

                if (!results.isEmpty()) {

                    StringBuilder sbu = new StringBuilder();
                    StringBuilder mhu = new StringBuilder();
                    boolean mhh = false;

                    SortUtil cc = new SortUtil(0, false);
                    Collections.sort(results, cc);

                    for (List<String> result : results) {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder mh = new StringBuilder();
                        switch (result.size()) {
                            case 3:
                                sb.append(String.format("%s", result.get(0)).substring(0, 8));
                                sb.append(String.join("", Collections.nCopies(12 - sb.length(), " ")));
                                sb.append(StringUtils.abbreviate(result.get(1), 36));
                                sb.append(String.join("", Collections.nCopies(47 - sb.length(), " ")));
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
                                sb.append(StringUtils.abbreviate(result.get(3), 50));
                                sb.append(String.format("%n"));
                                break;

                            case 6:
                                int padding = 0;
                                if (!mhh) {
                                    mh.append("");
                                    mh.append(String.join("", Collections.nCopies(6 - mh.length(), " ")));
                                    mh.append("");
                                    mh.append(String.join("", Collections.nCopies(14 - mh.length(), " ")));
                                    mh.append("Min");
                                    mh.append(String.join("", Collections.nCopies(27 - mh.length(), " ")));
                                    mh.append("Max");
                                    mh.append(String.join("", Collections.nCopies(42 - mh.length(), " ")));
                                    mh.append("Average");
                                    mh.append(String.join("", Collections.nCopies(57 - mh.length(), " ")));
                                    mh.append(String.format("%n"));
                                    mhh = true;
                                    padding = mh.length();
                                }
                                mh.append(result.get(0));
                                mh.append(String.join("", Collections.nCopies(6 - mh.length() + padding, " ")));
                                mh.append(result.get(1));
                                mh.append(String.join("", Collections.nCopies(14 - mh.length() + padding, " ")));
                                mh.append(result.get(2));
                                mh.append(String.join("", Collections.nCopies(27 - mh.length() + padding, " ")));
                                mh.append(result.get(3));
                                mh.append(String.join("", Collections.nCopies(42 - mh.length() + padding, " ")));
                                mh.append(result.get(4));
                                mh.append(String.join("", Collections.nCopies(57 - mh.length() + padding, " ")));
                                mh.append(String.format("%n"));
                                break;

                            default:
                                break;
                        }

                        if (sbu.length() + sb.length() < 1900) {
                            sbu.append(sb.toString());
                        }

                        mhu.append(mh.toString());
                    }

                    if (mhu.length() > 0) {
                        StringBuilder mhBuilder = new StringBuilder();
                        mhBuilder.append(String.format("Market History for %s", pageTitle));

                        mhBuilder.append("```haskell");
                        mhBuilder.append(String.format("%n"));
                        mhBuilder.append(mhu.toString());
                        mhBuilder.append("```");
                        resultList.add(0, mhBuilder.toString());
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
                addFooter(object, "pc", page, pageNum);

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

}

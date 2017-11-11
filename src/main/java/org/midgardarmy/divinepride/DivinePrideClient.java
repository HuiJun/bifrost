package org.midgardarmy.divinepride;

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

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import org.midgardarmy.utils.ConfigUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class DivinePrideClient {

    private static final Logger logger = LoggerFactory.getLogger(DivinePrideClient.class);

    public static final String DIVINE_PRIDE_API_KEY = ConfigUtils.get("divinepride.apiKey");
    public static final String BASEURL = "https://www.divine-pride.net/";

    public static final String API = "api/database/";
    public static final String SEARCH = "database/";

    public static final Map<String, String> ENDPOINTMAP;
    static {
        ENDPOINTMAP = new HashMap<>();
        ENDPOINTMAP.put("e", "Experience");
        ENDPOINTMAP.put("a", "Achievement");
        ENDPOINTMAP.put("b", "Buff");
        ENDPOINTMAP.put("i", "Item");
        ENDPOINTMAP.put("ma", "Map");
        ENDPOINTMAP.put("m", "Monster");
        ENDPOINTMAP.put("n", "NpcIdentity");
        ENDPOINTMAP.put("q", "Quest");
        ENDPOINTMAP.put("s", "Skill");
        ENDPOINTMAP.put("t", "Title");
    }

    public static synchronized List<EmbedObject> getByName(String action, String id) {
        String actionParam = ENDPOINTMAP.get(action).toLowerCase();
        List<EmbedObject> resultList = new ArrayList<>();
        try {
            URIBuilder b = new URIBuilder(BASEURL);
            b.setPath(SEARCH + actionParam);
            b.setParameter("Name", id);
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
            return getById(action, ids);
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
        return resultList;
    }

    public static synchronized List<EmbedObject> getById(String action, List<String> ids) {
        String actionParam = ENDPOINTMAP.get(action).toLowerCase();
        List<EmbedObject> resultList = new ArrayList<>();
        for (String id : ids) {
            try {
                URIBuilder b = new URIBuilder(BASEURL);
                b.setPath(API + actionParam + ((id != null) ? "/" + id : ""));
                HttpResponse<JsonNode> results = apiCall(b.toString());
                Class clazz = Class.forName("org.midgardarmy.divinepride.templates." + ENDPOINTMAP.get(action) + "Template");
                Method method = clazz.getMethod("apply", JsonNode.class);
                EmbedBuilder object = (EmbedBuilder) method.invoke(null, results.getBody());
                resultList.add(object.build());
            } catch (Exception e) {
                logger.debug(e.getMessage());
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
        List<String> result = new ArrayList<>();
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//div[@id=\"target-1\"]//table/tbody/tr/td/a/@href";
            NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                result.add(extractIDString(nodes.item(i).getNodeValue()));
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }

        return result;
    }

    private static HttpResponse<String> getHTML(String url) throws UnirestException {
        return Unirest.get(url)
                .header("accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,*/*;q=0.5")
                .asString();
    }

    public static HttpResponse<JsonNode> apiCall(String url) throws UnirestException {
        return Unirest.get(url)
                .header("accept", "application/json")
                .queryString("apiKey", DIVINE_PRIDE_API_KEY)
                .asJson();
    }

    private static String extractIDString(String str) {
        return str.split("/")[3];
    }
}

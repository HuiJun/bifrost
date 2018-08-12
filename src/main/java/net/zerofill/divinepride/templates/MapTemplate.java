package net.zerofill.divinepride.templates;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import net.zerofill.divinepride.DivinePrideClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import sx.blah.discord.util.EmbedBuilder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class MapTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MapTemplate.class);

    private static final String ID = "mapname";

    public static final List<NameValuePair> MAP;
    static {
        MAP = new ArrayList<>();
        MAP.add(new BasicNameValuePair("Page", "1"));
    }

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();
        String mapID = root.getString(ID);
        String mapName = root.optString("name", "Unknown");

        if (logger.isDebugEnabled()) {
            logger.debug(json.toString());
        }

        JSONArray spawnArr = root.optJSONArray("spawn");
        JSONArray npcArr = root.optJSONArray("npcs");

        StringBuilder spawnList = new StringBuilder();
        if (spawnArr != null) {
            for (int i = 0; i < spawnArr.length(); i++) {
                JSONObject spawn = spawnArr.getJSONObject(i);
                spawnList.append(String.format("%s (%d) x %d", spawn.getString("respawnTime"), spawn.getInt("monsterId"), spawn.getInt("amount")));
                spawnList.append(String.format("%n"));
            }
        }

        StringBuilder npcList = new StringBuilder();
        StringBuilder warpList = new StringBuilder();

        if (npcArr != null) {
            for (int i = 0; i < npcArr.length(); i++) {
                JSONObject npc = npcArr.getJSONObject(i);
                switch (npc.getString("type")) {
                    case "npc":
                        npcList.append(String.format("%s (%d)", npc.optString("name", "No Name"), npc.getInt("id")));
                        npcList.append(String.format("`/navi %s %d/%d`", npc.optString("mapname"), npc.getInt("x"), npc.getInt("y")));
                        npcList.append(String.format("%n"));
                        break;
                    case "warp":
                        warpList.append(String.format("`/navi %s %d/%d`", npc.optString("mapname"), npc.getInt("x"), npc.getInt("y")));
                        warpList.append(String.format("%n"));
                        break;
                    default:
                        break;
                }
            }
        }

        builder.withColor(255, 0, 0);
        builder.withTitle(String.format("%s (%s)", mapName, mapID));
        builder.withThumbnail(String.format("https://www.divine-pride.net/img/map/raw/%s", mapID));
        builder.withImage(String.format("https://www.divine-pride.net/img/map/original/%s", mapID));

        builder.withAuthorName("DivinePride.net");
        builder.withAuthorIcon("https://static.divine-pride.net/images/logo.png");
        builder.withAuthorUrl(String.format("https://www.divine-pride.net/database/map/%s", mapID));

        if (spawnList.toString().trim().length() > 0) {
            builder.appendField("Spawns", spawnList.toString(), false);
        }
        if (npcList.toString().trim().length() > 0) {
            builder.appendField("NPCs", npcList.toString(), false);
        }
        if (warpList.toString().trim().length() > 0) {
            builder.appendField("Warps", warpList.toString(), false);
        }

        return builder;
    }

    private static List<String> getSpawns(String mapName) {
        List<String> results = new ArrayList<>();

        try {
            URIBuilder b = new URIBuilder(DivinePrideClient.BASEURL);
            String actionParam = DivinePrideClient.ENDPOINTMAP.get("m").toLowerCase();
            b.setPath(String.format("%s%s", DivinePrideClient.SEARCH, actionParam));

            List<NameValuePair> mapList = new ArrayList<>();
            mapList.addAll(MAP);
            mapList.add(new BasicNameValuePair("Map", mapName));
            b.addParameters(mapList);

            HttpResponse<String> response = DivinePrideClient.getHTML(b.build().toString());

            Tidy tidy = new Tidy();
            tidy.setQuiet(true);
            tidy.setShowWarnings(false);
            tidy.setShowErrors(0);
            tidy.setDropEmptyParas(true);
            tidy.setMakeClean(true);
            tidy.setXmlTags(true);
            tidy.setXmlOut(true);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getBody().getBytes("UTF-8"));
            Document xmlDocument = tidy.parseDOM(inputStream, null);
            List<List<String>> data = extractData(xmlDocument);
            for (List<String> each : data) {
                logger.debug(String.format("%s", String.join(",", each)));
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getSpawns Error: ", e);
            }
        }

        return results;
    }

    private static List<List<String>> extractData(Document xmlDocument) {
        List<List<String>> result = new ArrayList<>();

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//table[contains(@class, 'fullwidth')]/tbody/tr";
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
                                    tableCell.append(spanValue);
                                    break;

                                case "a":
                                    tableCell.append(node.getFirstChild().getNodeValue().trim());
                                    break;

                                default:
                                    if (node.getChildNodes().getLength() > 0) {
                                        List<String> newData = new ArrayList<>();
                                        for (int k = 0; k < node.getChildNodes().getLength(); k++) {
                                            Node child = node.getChildNodes().item(k);
                                            if (child.getNodeName().equals("a")) {
                                                if (logger.isDebugEnabled()) {
                                                    logger.debug(child.getNodeValue().trim());
                                                }
                                                newData.add(child.getNodeValue().trim());
                                            }
                                        }
                                        tableCell.append(String.join(",", newData));
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

    private MapTemplate() {}

}

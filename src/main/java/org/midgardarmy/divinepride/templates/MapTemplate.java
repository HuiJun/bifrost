package org.midgardarmy.divinepride.templates;

import com.mashape.unirest.http.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.EmbedBuilder;

public class MapTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MapTemplate.class);

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();

        if (logger.isDebugEnabled()) {
            logger.debug(json.toString());
        }

        JSONArray spawnArr = root.optJSONArray("spawn");
        JSONArray npcArr = root.optJSONArray("npcs");

        StringBuilder spawnList = new StringBuilder();
        if (spawnArr != null) {
            for (int i = 0; i < spawnArr.length(); i++) {
                JSONObject spawn = spawnArr.getJSONObject(i);
                spawnList.append(String.format("%s (%s)", spawn.getString("name"), spawn.getInt("mapname")));
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
                        npcList.append(String.format("%s", cleanName(npc.getString("name"))));
                        npcList.append(String.format("%n"));
                        break;
                    case "warp":
                        warpList.append(String.format("%s", cleanName(npc.getString("name"))));
                        warpList.append(String.format("%n"));
                        break;
                    default:
                        break;
                }
            }
        }

        builder.withColor(255, 0, 0);
        builder.withThumbnail(String.format("https://www.divine-pride.net/img/map/original/%s.png", root.getString("id")));

        builder.withAuthorName("DivinePride.net");
        builder.withAuthorIcon("https://static.divine-pride.net/images/logo.png");
        builder.withAuthorUrl(String.format("https://www.divine-pride.net/database/map/%s", root.getString("id")));

        builder.appendField("NPCs", npcList.toString(), false);
        builder.appendField("Warps", warpList.toString(), false);

        return builder;
    }

    private static String cleanName(String name) {
        return name.substring(0, name.indexOf('#'));
    }

    private MapTemplate() {}
}

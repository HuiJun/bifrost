package org.midgardarmy.divinepride.templates;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.sun.org.apache.xpath.internal.operations.Div;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.midgardarmy.divinepride.DivinePrideClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.EmbedBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MonsterTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MonsterTemplate.class);

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();
        JSONObject stats = root.optJSONObject("stats");
        JSONArray dropsArr = root.optJSONArray("drops");
        List<JsonNode> drops = getDrops(dropsArr);
        JSONArray spawnArr = root.optJSONArray("spawn");

        StringBuilder dropList = new StringBuilder();
        for (int i = 0; i < drops.size(); i++) {
            JsonNode drop = drops.get(i);
            JSONObject dropObj = drop.getObject();
            JSONObject dropContext = dropsArr.optJSONObject(i);
            String dropChance = Integer.toString(dropContext.optInt("chance") / 100);
            dropList.append(String.format("%s (%d) - %s%%", dropObj.optString("name"), dropObj.optInt("id"), dropChance));
            dropList.append(String.format("%n"));
        }

        StringBuilder spawnList = new StringBuilder();
        for (int i = 0; i < spawnArr.length(); i++) {
            JSONObject spawn = spawnArr.getJSONObject(i);
            spawnList.append(String.format("%s (%d)", spawn.getString("mapname"), spawn.getInt("amount")));
            spawnList.append(String.format("%n"));
        }

        if (logger.isDebugEnabled()) {
            logger.debug(json.toString());
        }

        builder.withColor(255, 0, 0);
        builder.withThumbnail(String.format("https://static.divine-pride.net/images/mobs/png/%d.png", root.getInt("id")));

        builder.withAuthorName("DivinePride.net");
        builder.withAuthorIcon("https://static.divine-pride.net/images/logo.png");
        builder.withAuthorUrl(String.format("https://www.divine-pride.net/database/monster/%d", root.getInt("id")));

        builder.appendField("ID", Integer.toString(root.getInt("id")), false);

        builder.appendField("Name", root.getString("name"), true);
        builder.appendField("Level", Integer.toString(stats.getInt("level")), true);

        builder.appendField("Health", Integer.toString(stats.getInt("health")), true);
        builder.appendField("Base Exp", Integer.toString(stats.getInt("baseExperience")), true);
        builder.appendField("Job Exp", Integer.toString(stats.getInt("jobExperience")), true);

        builder.appendField("STR", Integer.toString(stats.getInt("str")), true);
        builder.appendField("AGI", Integer.toString(stats.getInt("agi")), true);
        builder.appendField("VIT", Integer.toString(stats.getInt("vit")), true);
        builder.appendField("INT", Integer.toString(stats.getInt("int")), true);
        builder.appendField("DEX", Integer.toString(stats.getInt("dex")), true);
        builder.appendField("LUK", Integer.toString(stats.getInt("luk")), true);

        builder.appendField("ATK", String.format("%d - %d", stats.getJSONObject("attack").getInt("minimum"), stats.getJSONObject("attack").getInt("maximum")), true);
        builder.appendField("DEF", Integer.toString(stats.getInt("defense")), true);
        builder.appendField("MDEF", Integer.toString(stats.getInt("magicDefense")), true);
        builder.appendField("ASPD", Integer.toString(stats.getInt("attackSpeed")), true);
        builder.appendField("HIT", Integer.toString(stats.getInt("hit")), true);
        builder.appendField("Drops", dropList.toString(), false);
        builder.appendField("Maps", spawnList.toString(), false);

        return builder;
    }

    private static List<JsonNode> getDrops(JSONArray drops) {
        List<JsonNode> results = new ArrayList<>();
        try {
            for (int i = 0; i < drops.length(); i++) {
                int id = drops.getJSONObject(i).getInt("itemId");
                URIBuilder b = new URIBuilder(DivinePrideClient.BASEURL);
                String actionParam = DivinePrideClient.ENDPOINTMAP.get("i").toLowerCase();
                b.setPath(DivinePrideClient.API + actionParam + ((id > 0) ? "/" + id : ""));
                HttpResponse<JsonNode> response = DivinePrideClient.apiCall(b.build().toString());
                results.add(response.getBody());
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        }
        return results;
    }

    private MonsterTemplate() {}
}

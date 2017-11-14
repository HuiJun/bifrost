package org.midgardarmy.divinepride.templates;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.midgardarmy.divinepride.DivinePrideClient;
import org.midgardarmy.utils.ConfigUtils;
import org.midgardarmy.utils.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.EmbedBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonsterTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MonsterTemplate.class);

    private static final Map<String, Float> rates;
    static {
        rates = new HashMap<>();
        rates.put("baseexp", Float.parseFloat(ConfigUtils.get("divinepride.baseexp")));
        rates.put("jobexp", Float.parseFloat(ConfigUtils.get("divinepride.jobexp")));
        rates.put("drop", Float.parseFloat(ConfigUtils.get("divinepride.drop")));
    }

    private static final Map<Integer, String> raceMap;
    static {
        raceMap = new HashMap<>();
        raceMap.put(0, "Formless");
        raceMap.put(1, "Undead");
        raceMap.put(2, "Brute");
        raceMap.put(3, "Plant");
        raceMap.put(4, "Insect");
        raceMap.put(5, "Fish");
        raceMap.put(6, "Demon");
        raceMap.put(7, "Demi-Human");
        raceMap.put(8, "Angel");
        raceMap.put(9, "Dragon");
        raceMap.put(10, "Player (default race for player)");
    }

    private static final Map<Integer, String> sizeMap;
    static {
        sizeMap = new HashMap<>();
        sizeMap.put(0, "Small");
        sizeMap.put(1, "Medium");
        sizeMap.put(2, "Large");
    }

    private static final Map<Integer, String> elementMap;
    static {
        List<Integer> levels = Arrays.asList(
                20,
                40,
                60,
                80
        );
        List<String> elements = Arrays.asList(
                "Neutral",
                "Water",
                "Earth",
                "Fire",
                "Wind",
                "Poison",
                "Holy",
                "Shadow",
                "Ghost",
                "Undead"
        );
        elementMap = new HashMap<>();
        for (int i = 0; i < levels.size(); i++) {
            Integer level = levels.get(i);
            for (int j = 0; j < elements.size(); j++) {
                elementMap.put(level + j, String.format("%s (Level %d)", elements.get(j), i + 1));
            }
        }
        elementMap.put(0, "Small");
    }

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();

        if (logger.isDebugEnabled()) {
            logger.debug(json.toString());
        }

        JSONObject stats = root.optJSONObject("stats");
        JSONArray dropsArr = root.optJSONArray("drops");
        JSONArray spawnArr = root.optJSONArray("spawn");

        StringBuilder dropList = new StringBuilder();
        if (dropsArr != null) {
            List<Integer> ids = new ArrayList<>();
            Map<Integer, Integer> dropChanceMap = new HashMap<>();
            for (int i = 0; i < dropsArr.length(); i++) {
                JSONObject current = dropsArr.getJSONObject(i);
                Integer itemId = current.getInt("itemId");
                if (current.optInt("chance") > 0) {
                    ids.add(itemId);
                    dropChanceMap.put(itemId, current.optInt("chance"));
                }
            }

            if (!ids.isEmpty()) {
                Map<Integer, Map<String, Object>> items = DataUtils.getItemsByIDs(ids);
                for (Map.Entry<Integer, Map<String, Object>> entry : items.entrySet()) {
                    Integer itemId = entry.getKey();
                    Map<String, Object> item = entry.getValue();
                    String dropChance = Float.toString(roundDecimal(modifyDropRate(dropChanceMap.getOrDefault(itemId, 0) / 100f)));
                    dropList.append(String.format("%s (%d) - %s%%", item.getOrDefault("name", "Unknown"), itemId, dropChance));
                    dropList.append(String.format("%n"));
                }

                ids.removeAll(items.keySet());
                if (!ids.isEmpty() && logger.isDebugEnabled()) {
                    String idsString = ids.toString().substring(1, ids.toString().length() - 1);
                    logger.debug(String.format("Could not find some elements : %s", String.join("", idsString)));
                }
            }
        }

        StringBuilder spawnList = new StringBuilder();
        if (spawnArr != null) {
            for (int i = 0; i < spawnArr.length(); i++) {
                JSONObject spawn = spawnArr.getJSONObject(i);
                spawnList.append(String.format("%s (%d)", spawn.getString("mapname"), spawn.getInt("amount")));
                spawnList.append(String.format("%n"));
            }
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
        builder.appendField("Base Exp", Integer.toString(modifyBaseExp(stats.getInt("baseExperience"))), true);
        builder.appendField("Job Exp", Integer.toString(modifyJobExp(stats.getInt("jobExperience"))), true);

        builder.appendField("STR", Integer.toString(stats.getInt("str")), true);
        builder.appendField("AGI", Integer.toString(stats.getInt("agi")), true);
        builder.appendField("VIT", Integer.toString(stats.getInt("vit")), true);
        builder.appendField("INT", Integer.toString(stats.getInt("int")), true);
        builder.appendField("DEX", Integer.toString(stats.getInt("dex")), true);
        builder.appendField("LUK", Integer.toString(stats.getInt("luk")), true);

        builder.appendField("ATK", String.format("%d - %d", stats.getJSONObject("attack").getInt("minimum"), stats.getJSONObject("attack").getInt("maximum")), true);
        builder.appendField("DEF", Integer.toString(stats.getInt("defense")), true);
        builder.appendField("MDEF", Integer.toString(stats.getInt("magicDefense")), true);
        builder.appendField("HIT", Integer.toString(stats.getInt("hit")), true);

        builder.appendField("ASPD", Float.toString(calculateASPD(stats.getInt("attackSpeed"))), true);
        builder.appendField("Hits / Sec", Float.toString(calculateHPS(stats.getInt("attackSpeed"))), true);

        builder.appendField("Race", raceMap.getOrDefault(stats.getInt("race"), "Unknown"), true);
        builder.appendField("Size", sizeMap.getOrDefault(stats.getInt("scale"), "Unknown"), true);

        builder.appendField("Element", elementMap.getOrDefault(stats.getInt("element"), "Unknown"), true);

        if (dropList.length() > 0) {
            builder.appendField("Drops", dropList.toString(), false);
        }
        if (spawnList.length() > 0) {
            builder.appendField("Maps", spawnList.toString(), false);
        }

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

    private static float calculateHPS(int delay) {
        return roundDecimal(1000f / delay);
    }

    private static float calculateASPD(int delay) {
        return roundDecimal(50f / (delay / 1000f));
    }

    private static float roundDecimal(float num) {
        BigDecimal bd = new BigDecimal(Float.toString(num));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private static float modifyDropRate(float base) {
        float mod = base * rates.get("drop");
        return mod <= 100f ? mod : 100.00f;
    }

    private static Integer modifyBaseExp(int base) {
        return Math.round(base * rates.get("baseexp"));
    }

    private static Integer modifyJobExp(int base) {
        return Math.round(base * rates.get("jobexp"));
    }

    private MonsterTemplate() {}

}

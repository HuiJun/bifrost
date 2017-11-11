package org.midgardarmy.divinepride.templates;

import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.EmbedBuilder;

public class MonsterTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(MonsterTemplate.class);

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();
        JSONObject stats = root.optJSONObject("stats");

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

        builder.appendField("ATK", Integer.toString(stats.getJSONObject("attack").getInt("maximum")), true);
        builder.appendField("DEF", Integer.toString(stats.getInt("defense")), true);
        builder.appendField("MDEF", Integer.toString(stats.getInt("magicDefense")), true);
        builder.appendField("ASPD", Integer.toString(stats.getInt("attackSpeed")), true);
        builder.appendField("HIT", Integer.toString(stats.getInt("hit")), true);

        return builder;
    }

    private MonsterTemplate() {}
}

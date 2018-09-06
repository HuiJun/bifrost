package net.zerofill.divinepride.templates;

import com.mashape.unirest.http.JsonNode;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.util.EmbedBuilder;

public class ItemTemplate extends BaseTemplate {

    private static final Logger logger = LoggerFactory.getLogger(ItemTemplate.class);

    static {
        replacements.clear();
        replacements.put("_?\\^[0-9a-fA-F]{6}", "");
    }

    public static EmbedBuilder apply(JsonNode json) {
        EmbedBuilder builder = new EmbedBuilder();
        JSONObject root = json.getObject();

        if (logger.isDebugEnabled()) {
            logger.debug(json.toString());
        }

        builder.withColor(0, 0, 255);

        String thumbnail = String.format("https://static.divine-pride.net/images/items/collection/%d.png", root.getInt("id"));
        if (isCard(root)) {
            thumbnail = String.format("https://static.divine-pride.net/images/items/cards/%d.png", root.getInt("id"));
        }

        builder.withImage(thumbnail);

        builder.withAuthorName("DivinePride.net");
        builder.withAuthorIcon("https://static.divine-pride.net/images/logo.png");
        builder.withAuthorUrl(String.format("https://www.divine-pride.net/database/item/%d", root.getInt("id")));

        builder.appendField("ID", Integer.toString(root.getInt("id")), false);
        builder.appendField("Name", root.getString("name"), false);
        builder.appendField("Description", clean(root.getString("description")), false);

        builder.withFooterText(root.optString("aegisName"));
        builder.withFooterIcon(String.format("https://static.divine-pride.net/images/items/item/%d.png", root.getInt("id")));

        return builder;
    }

    private static boolean isCard(JSONObject json) {
        String aegisName = json.optString("aegisName");
        if (aegisName == null || aegisName.isEmpty()) {
            aegisName = json.optString("name");
        }
        return aegisName.substring(aegisName.length() > 4 ? aegisName.length() - 4 : 0).equalsIgnoreCase("CARD");
    }

    private ItemTemplate() {}

}

package org.midgardarmy.shivtr.actions;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.midgardarmy.shivtr.ShivtrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Activities extends ShivtrClient {

    private static final Logger logger = LoggerFactory.getLogger(Activities.class);

    private static final String FORUMS = "forums";
    private static final String FORUM_THREADS = "forum_threads";
    private static final String FORUM_LINK = BASEURL + "/" + FORUM_THREADS + "/%d";
    private static String TOKEN = postLogin();

    public static List<EmbedObject> getForumsList() {
        if (TOKEN == null || TOKEN.isEmpty()) {
            TOKEN = postLogin();
        }

        List<EmbedObject> results = new ArrayList<>();

        StringBuilder url = new StringBuilder();
        url.append(BASEURL);
        url.append(FORUM_THREADS);
        url.append(".json");

        if (logger.isDebugEnabled()) {
            logger.debug("Forums url: " + url.toString());
        }
        try {
            JsonNode response = Unirest.get(url.toString())
                    .header("accept", "application/json")
                    .queryString("token", TOKEN)
                    .asJson()
                    .getBody();

            Map<Integer, String> forumMap = getForumMap(response.getObject().getJSONArray(FORUMS));
            JSONArray threads = response.getObject().getJSONArray(FORUM_THREADS);

            Comparator<JSONObject> comp = (JSONObject o1, JSONObject o2) -> (o2.optLong("id", 0L) > o1.optLong("id", 0L)) ? 1 : -1;

            TreeSet<JSONObject> jsonTree = new TreeSet<>(comp);
            for (int i = 0; i < threads.length(); i++) {
                jsonTree.add(threads.getJSONObject(i));
            }

            int count = 0;

            for (JSONObject current : jsonTree) {
                if (count == 4) {
                    break;
                }
                EmbedBuilder builder = new EmbedBuilder();

                URIBuilder b = new URIBuilder(String.format(FORUM_LINK, current.getInt("id")));
                b.addParameter("last_or_unread", "true");

                builder.withTitle(current.getString("subject"));
                builder.withUrl(b.build().toString());
                builder.appendField("Forum", forumMap.getOrDefault(current.getInt("forum_id"), "Unknown"), true);
                builder.appendField("Post Count", Integer.toString(current.getInt("forum_posts_count")), true);

                results.add(builder.build());
                count++;
            }
        } catch (UnirestException | URISyntaxException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getForums Error: ", e);
            }
        }

        return results;
    }

    private static Map<Integer, String> getForumMap(JSONArray json) {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject forum = json.getJSONObject(i);
            result.put(forum.getInt("id"), forum.getString("name"));
        }
        return result;
    }

}

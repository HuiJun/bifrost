package org.midgardarmy.shivtr.actions;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.midgardarmy.shivtr.ShivtrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Applications extends ShivtrClient{

    private static final Logger logger = LoggerFactory.getLogger(Applications.class);

    private static final String APPLICATION_LIST = "site_applications";
    private static final String SUBCLASSES = "game_subclasses";
    private static final String CLASSES = "game_classes";
    private static final String TOKEN = postLogin();

    private static DateTimeFormatter inputDate = DateTimeFormatter.ISO_DATE_TIME;
    private static SimpleDateFormat outputDate = new SimpleDateFormat("yyyy-MM-dd");

    public static List<EmbedObject> getApplicationList() {
        if (TOKEN == null || TOKEN.isEmpty()) {
            postLogin();
        }
        StringBuilder url = new StringBuilder();
        url.append(BASEURL);
        url.append(APPLICATION_LIST);
        url.append(".json");
        try {
            JsonNode response = Unirest.get(url.toString())
                    .header("accept", "application/json")
                    .queryString("token", TOKEN)
                    .asJson()
                    .getBody();
            logger.debug(response.toString());
            JSONArray applications = response.getObject().getJSONArray(APPLICATION_LIST);

            //Map<Integer, String> classesMap = convertJSONArrayToMap(response.getObject().getJSONArray(CLASSES));
            Map<Integer, String> subClassesMap = convertJSONArrayToMap(response.getObject().getJSONArray(SUBCLASSES));

            StringBuilder sbu = new StringBuilder();
            for (int i = 0; i < applications.length(); i++) {
                JSONObject application = applications.getJSONObject(i);
                StringBuilder sb = new StringBuilder();

                TemporalAccessor accessor = inputDate.parse(application.optString("created_on"));
                Date date = Date.from(Instant.from(accessor));

                sb.append(application.optString("id"));
                sb.append(String.join("", Collections.nCopies(8 - sb.length(), " ")));
                sb.append(application.optString("name"));
                sb.append(String.join("", Collections.nCopies(26 - sb.length(), " ")));
                sb.append(subClassesMap.getOrDefault(application.optInt("game_subclass_id"), "Unknown"));
                sb.append(String.join("", Collections.nCopies(40 - sb.length(), " ")));
                sb.append(outputDate.format(date));
                sb.append(String.format("%n"));

                sbu.append(sb.toString());
                logger.debug(sb.toString());
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.withDescription("```haskell");
            builder.appendDescription(String.format("%n"));
            builder.appendDescription(sbu.toString());
            builder.appendDescription(String.format("%n"));
            builder.appendDescription("```");
            return Arrays.asList(builder.build());
        } catch (UnirestException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getApplications Error: ", e);
            }
        }
        return new ArrayList<>();
    }

}

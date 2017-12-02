package org.midgardarmy.shivtr;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.midgardarmy.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.internal.json.objects.EmbedObject;

import java.util.ArrayList;
import java.util.List;

public class ShivtrClient {

    private static final Logger logger = LoggerFactory.getLogger(ShivtrClient.class);

    private static final String SHIVTR_USERNAME = ConfigUtils.get("shivtr.user");
    private static final String SHIVTR_PASSWORD = ConfigUtils.get("shivtr.pass");

    private static final String BASEURL = ConfigUtils.get("shivtr.url");
    private static final String LOGIN = "users/sign_in.json";

    private static final String TOKEN = postLogin();

    public static List<EmbedObject> getApplications() {
        postLogin();
        if (TOKEN == null && TOKEN.isEmpty()) {

        }
        StringBuilder url = new StringBuilder();
        url.append(BASEURL);
        url.append("site_applications.json");
        try {
            HttpResponse<JsonNode> response = Unirest.get(url.toString())
                    .header("accept", "application/json")
                    .queryString("token", TOKEN)
                    .asJson();
        } catch (UnirestException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("getApplications Error: ", e);
            }
        }
        return new ArrayList<EmbedObject>();
    }

    private static String postLogin() {
        StringBuilder url = new StringBuilder();
        url.append(BASEURL);
        url.append(LOGIN);
        JSONObject json = new JSONObject().put("user", new JSONObject().put("email", SHIVTR_USERNAME).put("password", SHIVTR_PASSWORD));

        try {
            return Unirest.post(url.toString())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asJson()
                    .getBody()
                    .getObject()
                    .optString("authentication_token", null);
        } catch (UnirestException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("PostLogin Error: ", e);
            }
        }

        return null;
    }

    private ShivtrClient() {}

}

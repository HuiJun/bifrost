package org.midgardarmy.shivtr;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.midgardarmy.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ShivtrClient {

    private static final Logger logger = LoggerFactory.getLogger(ShivtrClient.class);

    private static final String SHIVTR_USERNAME = ConfigUtils.get("shivtr.user");
    private static final String SHIVTR_PASSWORD = ConfigUtils.get("shivtr.pass");
    private static final String LOGIN = "users/sign_in.json";

    protected static final String BASEURL = ConfigUtils.get("shivtr.url");

    protected static String postLogin() {
        StringBuilder url = new StringBuilder();
        url.append(BASEURL);
        url.append(LOGIN);
        JSONObject json = new JSONObject().put("user", new JSONObject().put("email", SHIVTR_USERNAME).put("password", SHIVTR_PASSWORD));

        try {
            JsonNode resultJson = Unirest.post(url.toString())
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asJson()
                    .getBody();

            String result = resultJson.getObject().optJSONObject("user_session").optString("authentication_token", null);

            if (logger.isDebugEnabled()) {
                logger.debug("Login URL: " + url.toString());
                logger.debug("Login body: " + json.toString());
                logger.debug("Login result: " + resultJson.toString());
                logger.debug("Login Token: " + result);
            }

            return result;
        } catch (UnirestException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("PostLogin Error: ", e);
            }
        }

        return null;
    }

    protected static Map<Integer, String> convertJSONArrayToMap(JSONArray json) {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject obj = json.getJSONObject(i);
            result.put(obj.getInt("id"), obj.getString("name"));
        }

        return result;
    }

    public ShivtrClient() {}

}

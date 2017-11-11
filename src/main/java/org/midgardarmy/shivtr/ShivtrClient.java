package org.midgardarmy.shivtr;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class ShivtrClient {
    private static final String SHIVTR_API_KEY = "";
    private static final String BASEURL = "http://midgardarmy.shivtr.com/";

    public static HttpResponse<JsonNode> getCall(String action, String id) throws UnirestException {
        return Unirest.get("http://httpbin.org/post")
                .header("accept", "application/json")
                .routeParam("apiKey", SHIVTR_API_KEY)
                .asJson();
    }
}

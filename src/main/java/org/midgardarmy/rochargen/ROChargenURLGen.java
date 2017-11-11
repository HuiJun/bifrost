package org.midgardarmy.rochargen;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.midgardarmy.utils.ConfigUtils;

public class ROChargenURLGen {

    private static final Logger logger = LoggerFactory.getLogger(ROChargenURLGen.class);

    private static final String BASEURL = ConfigUtils.get("rochargen.url") + "%s/%d/%d";
    private static final int BG_MIN = Integer.parseInt(ConfigUtils.get("rochargen.bg.min"));
    private static final int BG_MAX = Integer.parseInt(ConfigUtils.get("rochargen.bg.max"));
    private static final int POS_MIN = Integer.parseInt(ConfigUtils.get("rochargen.pos.min"));
    private static final int POS_MAX = Integer.parseInt(ConfigUtils.get("rochargen.pos.max"));

    public static String generateSig(String charName) {
        Random rand = new Random();
        int bgID = rand.nextInt(BG_MAX) + BG_MIN;
        int posID = rand.nextInt(POS_MAX) + POS_MIN;
        return generateSig(charName, bgID, posID);
    }

    private static String generateSig(String charName, int bgID, int posID) {
        try {
            URIBuilder b = new URIBuilder(String.format(BASEURL, encodeURIComponent(charName), bgID, posID));
            return b.toString();
        } catch (URISyntaxException e) {
            logger.debug(e.getLocalizedMessage());
        }
        return null;
    }

    private static String encodeURIComponent(String str) {
        String result;

        try {
            result = URLEncoder.encode(str, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = str;
        }

        return result;
    }

    private ROChargenURLGen() {
        // do nothing
    }
}

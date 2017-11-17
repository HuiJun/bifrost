package org.midgardarmy.rochargen;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.utils.ConfigUtils;

public class ROChargenURLGen {

    private static final Logger logger = LoggerFactory.getLogger(ROChargenURLGen.class);

    private static final String NEWSIG = "newsig/";
    private static final String CHAR = "char/";

    private static final String SIGURL = ConfigUtils.get("rochargen.url") + NEWSIG + "%s/%d/%d";
    private static final String CHARURL = ConfigUtils.get("rochargen.url") + CHAR + "%s/%d/%d";

    private static final int BG_MIN = Integer.parseInt(ConfigUtils.get("rochargen.bg.min"));
    private static final int BG_MAX = Integer.parseInt(ConfigUtils.get("rochargen.bg.max"));
    private static final int POS_MIN = Integer.parseInt(ConfigUtils.get("rochargen.pos.min"));
    private static final int POS_MAX = Integer.parseInt(ConfigUtils.get("rochargen.pos.max"));

    private static final int ROTATE_MIN = Integer.parseInt(ConfigUtils.get("rochargen.rotate.min"));
    private static final int ROTATE_MAX = Integer.parseInt(ConfigUtils.get("rochargen.rotate.max"));
    private static final int POSE_MIN = Integer.parseInt(ConfigUtils.get("rochargen.pose.min"));
    private static final int POSE_MAX = Integer.parseInt(ConfigUtils.get("rochargen.pose.max"));

    public static EmbedObject generateSig(String charName) {
        Random rand = new Random();
        int bgID = rand.nextInt(BG_MAX) + BG_MIN;
        int posID = rand.nextInt(POS_MAX) + POS_MIN;
        return generateSig(charName, bgID, posID);
    }

    private static EmbedObject generateSig(String charName, int bgID, int posID) {
        try {
            URIBuilder b = new URIBuilder(String.format(SIGURL, encodeURIComponent(charName), bgID, posID));
            return buildEmbed(b.build());
        } catch (URISyntaxException e) {
            logger.debug(e.getLocalizedMessage());
        }
        return null;
    }

    public static EmbedObject generateChar(String charName) {
        Random rand = new Random();
        int poseID = rand.nextInt(POSE_MAX) + POSE_MIN;
        int rotationID = rand.nextInt(ROTATE_MAX) + ROTATE_MIN;
        return generateChar(charName, poseID, rotationID);
    }

    private static EmbedObject generateChar(String charName, int poseID, int rotationID) {
        try {
            URIBuilder b = new URIBuilder(String.format(CHARURL, encodeURIComponent(charName), poseID, rotationID));
            return buildEmbed(b.build());
        } catch (URISyntaxException e) {
            logger.debug(e.getLocalizedMessage());
        }
        return null;
    }

    private static EmbedObject buildEmbed(URI url) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(0, 255, 0);
        builder.withDescription(url.toString());
        builder.withImage(url.toString());
        return builder.build();
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

    private ROChargenURLGen() {}

}

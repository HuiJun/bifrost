package org.midgardarmy.rochargen;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import org.midgardarmy.utils.ConfigUtils;

public class ROChargenURLGen {

    private static final Logger logger = LoggerFactory.getLogger(ROChargenURLGen.class);

    private static final String BASEURL = ConfigUtils.get("rochargen.url");

    private static final String SIGURL = BASEURL + (!BASEURL.substring(BASEURL.length() - 1).equals("/") ? "/" : "" ) + "newsig/%s/%d/%d";
    private static final String CHARURL = BASEURL + (!BASEURL.substring(BASEURL.length() - 1).equals("/") ? "/" : "" ) + "char/%s/%d/%d";
    private static final String AVATARURL = BASEURL + (!BASEURL.substring(BASEURL.length() - 1).equals("/") ? "/" : "" ) + "avatar/%s";

    private static final int BG_MIN = ConfigUtils.getInt("rochargen.bg.min");
    private static final int BG_MAX = ConfigUtils.getInt("rochargen.bg.max");
    private static final int POS_MIN = ConfigUtils.getInt("rochargen.pos.min");
    private static final int POS_MAX = ConfigUtils.getInt("rochargen.pos.max");

    private static final int ROTATE_MIN = ConfigUtils.getInt("rochargen.rotate.min");
    private static final int ROTATE_MAX = ConfigUtils.getInt("rochargen.rotate.max");
    private static final int POSE_MIN = ConfigUtils.getInt("rochargen.pose.min");
    private static final int POSE_MAX = ConfigUtils.getInt("rochargen.pose.max");

    public static EmbedObject generateSig(String charName) {
        int bgID = ThreadLocalRandom.current().nextInt(BG_MIN, BG_MAX + 1);
        int posID = ThreadLocalRandom.current().nextInt(POS_MIN, POS_MAX + 1);
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
        int poseID = ThreadLocalRandom.current().nextInt(POSE_MIN, POSE_MAX + 1);
        int rotationID = ThreadLocalRandom.current().nextInt(ROTATE_MIN, ROTATE_MAX + 1);
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

    public static EmbedObject generateAvatar(String charName) {
        try {
            URIBuilder b = new URIBuilder(String.format(AVATARURL, encodeURIComponent(charName)));
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

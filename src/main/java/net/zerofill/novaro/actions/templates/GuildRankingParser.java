package net.zerofill.novaro.actions.templates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class GuildRankingParser implements BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(GuildRankingParser.class);

    public String execute(List<List<String>> input) {
        StringBuilder mhu = new StringBuilder();
        boolean mhh = false;
        for (List<String> result : input) {
            switch (result.size()) {
                case 7:
                case 8:
                    StringBuilder mh = new StringBuilder();

                    int padding = 0;
                    if (!mhh) {
                        mh.append("Rank");
                        mh.append(String.join("", Collections.nCopies(6 - mh.length(), " ")));
                        mh.append("Guild Name");
                        mh.append(String.join("", Collections.nCopies(23 - mh.length(), " ")));
                        mh.append("Castles");
                        mh.append(String.join("", Collections.nCopies(32 - mh.length(), " ")));
                        mh.append("Experience");
                        mh.append(String.join("", Collections.nCopies(51 - mh.length(), " ")));
                        mh.append(String.format("%n"));

                        padding = mh.length();
                        mhh = true;
                    }

                    mh.append(result.get(0));
                    mh.append(String.join("", Collections.nCopies(6 - mh.length() + padding, " ")));
                    mh.append(result.get(2));
                    mh.append(String.join("", Collections.nCopies(23 - mh.length() + padding, " ")));
                    mh.append(result.get(4));
                    mh.append(String.join("", Collections.nCopies(32 - mh.length() + padding, " ")));
                    mh.append(result.get(7));
                    mh.append(String.join("", Collections.nCopies(51 - mh.length() + padding, " ")));
                    mh.append(String.format("%n"));

                    mhu.append(mh.toString());
                    break;

                default:
                    break;
            }
        }

        return mhu.toString();
    }
}

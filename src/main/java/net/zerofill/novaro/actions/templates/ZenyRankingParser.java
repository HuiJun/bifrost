package net.zerofill.novaro.actions.templates;

import java.util.Collections;
import java.util.List;

public class ZenyRankingParser implements BaseParser {
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
                        mh.append("Name");
                        mh.append(String.join("", Collections.nCopies(30 - mh.length(), " ")));
                        mh.append("Zeny");
                        mh.append(String.join("", Collections.nCopies(43 - mh.length(), " ")));
                        mh.append(String.format("%n"));

                        padding = mh.length();
                        mhh = true;
                    }

                    mh.append(result.get(0));
                    mh.append(String.join("", Collections.nCopies(6 - mh.length() + padding, " ")));
                    mh.append(result.get(1));
                    mh.append(String.join("", Collections.nCopies(30 - mh.length() + padding, " ")));
                    mh.append(result.get(2));
                    mh.append(String.join("", Collections.nCopies(43 - mh.length() + padding, " ")));
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

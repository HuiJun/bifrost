package net.zerofill.roster.actions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import net.zerofill.roster.Roster;
import net.zerofill.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetAtt extends Roster {

    private static final Logger logger = LoggerFactory.getLogger(SetAtt.class);

    private static final String APPLICATION_NAME = "Discord Bot";

    public static String set(String woe, String name, String val) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = ConfigUtils.get(String.format("roster.%s.sid", woe));
        final String nameRange = String.format("%1$s%2$s:%1$s100", ConfigUtils.get(String.format("roster.%s.namecol", woe)), ConfigUtils.get(String.format("roster.%s.startrow", woe)));
        final String colRange = String.format("%1$s%2$s:%1$s100", ConfigUtils.get(String.format("roster.%s.attcol", woe)), ConfigUtils.get(String.format("roster.%s.startrow", woe)));
        final List<String> range = Arrays.asList(
                nameRange,
                colRange
        );

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sp.getSheets();
        if (!sheets.isEmpty()) {

            BatchGetValuesResponse response = service.spreadsheets().values()
                    .batchGet(spreadsheetId)
                    .setRanges(range)
                    .execute();

            List<ValueRange> values = response.getValueRanges();
            if (values == null || values.isEmpty()) {
                logger.debug("No data found.");
            } else {

                List<List<Object>> names = values.get(0).getValues();
                List<List<Object>> atts = values.get(1).getValues();

                for (int i = 0; i < names.size(); i++) {
                    List<Object> row = names.get(i);

                    if (atts == null) {
                        atts = new ArrayList<>();
                    }

                    if (atts.size() < i + 1) {
                        atts.add(i, new ArrayList<>());
                    }

                    if (row.get(0).toString().toLowerCase().startsWith(name.toLowerCase())) {

                        if (atts == null || atts.get(i) == null || atts.get(i).isEmpty()) {
                            atts.get(i).add(0, capitalize(val));
                        } else {
                            atts.get(i).set(0, capitalize(val));
                        }

                        ValueRange body = new ValueRange();
                        body.setValues(atts);

                        Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, colRange, body);
                        request.setValueInputOption("USER_ENTERED");

                        try {
                            UpdateValuesResponse result = request.execute();
                            if (result.size() > 0) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(String.format("Updated %d cells", result.getUpdatedCells()));
                                }
                                return String.format("Try again. I refreshed the token :sweat_smile:%nJK, I have successfully updated %s's attendance to %s.", name, capitalize(val));
                            }
                        } catch (Exception e) {
                            logger.debug(e.getLocalizedMessage());
                        }
                    }
                }

                return helpMessage(name);
            }
        }
        return "Error. Please check the logs";
    }

    private static String helpMessage(String name) {
        StringBuilder message = new StringBuilder();
        message.append("Your character is not on the roster. Please add it with the following:");
        message.append(String.format("%n"));
        message.append(String.format("`%saddatt %s ROLE_CODE`", ConfigUtils.get("discord.bot.prefix"), name));
        message.append(String.format("%n"));
        message.append("Where `ROLE_CODE` is the code of one of the following:");
        message.append(Roster.rolesMenu());
        return message.toString();
    }
}

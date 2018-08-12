package net.zerofill.roster.actions;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import net.zerofill.roster.Roster;
import net.zerofill.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAtt extends Roster {

    private static final Logger logger = LoggerFactory.getLogger(SetAtt.class);

    private static final String APPLICATION_NAME = "Discord Bot";

    public static String add(String woe, String name, String role) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = ConfigUtils.get(String.format("roster.%s.sid", woe));
        final String range = ConfigUtils.get(String.format("roster.%s.range", woe));

        String normalizedRole = Roster.normalizeRole(role);

        if (normalizedRole == null) {
            return helpMessage();
        }

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sp.getSheets();
        if (!sheets.isEmpty()) {

            List<Object> addRow = Arrays.asList(
                    name,
                    normalizedRole
            );

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                logger.error("No data found.");
            } else {
                for (List row : values) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("%s, %s", row.get(0), row.get(1)));
                    }
                    if (row.get(0).toString().toLowerCase().startsWith(name.toLowerCase())) {
                        return ("Duplicate row found. What are you doing idiot?");
                    }
                }

                values.add(addRow);

                ValueRange body = new ValueRange();
                body.setValues(values);

                logger.debug(range);
                Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, range, body);
                request.setValueInputOption("USER_ENTERED");

                try {
                    UpdateValuesResponse result = request.execute();
                    if (result.size() > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Added %d row", result.getUpdatedCells()));
                        }
                        return String.format("Try again. I refreshed the token :sweat_smile:%nJK, I have successfully added %1$s %2$s. Please set attendance with `%3$ssetatt %1$s Yes/No`", name, normalizedRole, ConfigUtils.get("discord.bot.prefix"));
                    }
                } catch (Exception e) {
                    logger.debug(e.getLocalizedMessage());
                }
            }
        }
        return "Error, please check logs";
    }

    private static String helpMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Please choose a code from the following list:");
        message.append(Roster.rolesMenu());
        return message.toString();
    }
}

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
import sx.blah.discord.handle.impl.obj.Role;
import sx.blah.discord.handle.obj.IRole;

public class ClearAtt extends Roster {

    private static final Logger logger = LoggerFactory.getLogger(SetAtt.class);

    private static final String APPLICATION_NAME = "Discord Bot";

    public static String clear(String woe, List<IRole> roles) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = ConfigUtils.get(String.format("roster.%s.sid", woe));
        final String range = String.format("%1$s%2$s:%1$s100", ConfigUtils.get(String.format("roster.%s.attcol", woe)), ConfigUtils.get(String.format("roster.%s.startrow", woe)));

        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Spreadsheet sp = service.spreadsheets().get(spreadsheetId).execute();
        List<Sheet> sheets = sp.getSheets();
        if (!sheets.isEmpty()) {

            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                return "Attendance table is already empty.";
            } else {
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i).isEmpty()) {
                        values.get(i).add(0, "");
                    } else {
                        values.get(i).set(0, "");
                    }
                }
                ValueRange body = new ValueRange();
                body.setValues(values);

                Sheets.Spreadsheets.Values.Update request = service.spreadsheets().values().update(spreadsheetId, range, body);
                request.setValueInputOption("USER_ENTERED");

                try {
                    UpdateValuesResponse result = request.execute();
                    if (result.size() > 0) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Updated %d cells", result.getUpdatedCells()));
                        }
                        StringBuilder message = new StringBuilder("Attendance cleared.");
                        message.append(String.format("%n"));
                        message.append(remind(woe, roles));
                        return message.toString();
                    }
                } catch (Exception e) {
                    logger.debug(e.getLocalizedMessage());
                }
            }
        }
        return "Error. Please check the logs";
    }

    private static String remind(String woe, List<IRole> roles) {
        String announce = ConfigUtils.getString(String.format("roster.%s.announce", woe));
        IRole mention = null;
        for(IRole role : roles) {
            if (role.getName().equalsIgnoreCase(announce)) {
                mention = role;
            }
        }
        return String.format("%s, please set your attendance for the coming war. Type the command %ssetatt <name> <yes/no> :sunglasses:", mention != null ? mention.mention() : "@here", ConfigUtils.getString("discord.bot.prefix"));
    }
}

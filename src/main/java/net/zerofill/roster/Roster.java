package net.zerofill.roster;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Roster {

    private static final Logger logger = LoggerFactory.getLogger(Roster.class);

    protected static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    protected static Map<String, String> WOE_ROLES = new HashMap<>();
    static {
        WOE_ROLES.put("RK", "Rune Knight");
        WOE_ROLES.put("WL", "Warlock");
        WOE_ROLES.put("RANG", "Ranger");
        WOE_ROLES.put("MECH", "Mechanic");
        WOE_ROLES.put("GX", "Guillotine Cross");
        WOE_ROLES.put("AB", "Arch Bishop");
        WOE_ROLES.put("RG", "Royal Guard");
        WOE_ROLES.put("SORC", "Sorcerer");
        WOE_ROLES.put("MINS", "Minstrel");
        WOE_ROLES.put("GYPS", "Gypsy");
        WOE_ROLES.put("GENE", "Genetic");
        WOE_ROLES.put("SC", "Shadow Chaser");
        WOE_ROLES.put("SURA", "Sura");
        WOE_ROLES.put("CAT", "Doram");
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials/ folder.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    protected static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleAuthorizationCodeFlow flow;
        try (InputStreamReader isr = new InputStreamReader(in)) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, isr);


            // Build flow and trigger user authorization request.
            flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                    .setAccessType("offline")
                    .build();
        }
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    protected static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    protected static String normalizeRole(String role) {
        if (WOE_ROLES.containsKey(role.toUpperCase())) {
            return WOE_ROLES.get(role.toUpperCase());
        }
        Iterator it = WOE_ROLES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry values = (Map.Entry) it.next();
            String value = values.getValue().toString();
            if (value.toLowerCase().contains(role.toLowerCase())) {
                return value;
            }
        }
        return null;
    }

    protected static String rolesMenu() {
        StringBuilder message = new StringBuilder();
        message.append("```");
        message.append(String.format("%n"));
        Iterator it = WOE_ROLES.entrySet().iterator();
        message.append("Code - Name");
        message.append(String.format("%n"));
        message.append(String.join("", Collections.nCopies(23, "-")));
        message.append(String.format("%n"));
        while (it.hasNext()) {
            Map.Entry current = (Map.Entry) it.next();
            String shortName = current.getKey().toString();
            String longName = current.getValue().toString();

            message.append(String.format("%s%s - %s", shortName, String.join("", Collections.nCopies(4 - shortName.length(), " ")), longName));
            message.append(String.format("%n"));
        }
        message.append("```");

        return message.toString();
    }
}

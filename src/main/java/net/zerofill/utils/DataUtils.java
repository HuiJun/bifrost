package net.zerofill.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.h2.tools.RunScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataUtils.class);

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:./bifrost;MODE=MySQL";
    private static final List<String> sqlFiles = new ArrayList<>(Arrays.asList(
            "data/item_db_re.sql",
            "data/mob_db_re.sql",
            "data/event_db.sql",
            "data/bifrost_db.sql"
    ));

    private static Connection conn = null;

    public static void loadAll() {
        for (String sqlFile : sqlFiles) {
            load(sqlFile);
        }
    }

    public static void load(String fileName) {
        getConn();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(fileName);

        if (resourceAsStream != null) {
            try (InputStreamReader isr = new InputStreamReader(resourceAsStream)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("#")) {
                            builder.append(line
                                    .replaceAll("\\\\'", "''")
                                    .replaceAll("(tiny|small|medium)int", "int")
                                    .replaceAll("text NOT NULL", "varchar(50) NOT NULL DEFAULT ''")
                            );
                        }
                    }
                    RunScript.execute(conn, new InputStreamReader(new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8.name()))));
                }
            } catch (Exception e) {
                logger.debug(e.getLocalizedMessage());
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    logger.debug(e.getLocalizedMessage());
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Data not loaded");
            }
        }
    }

    private static void getConn() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName(DB_DRIVER);
                conn = DriverManager.getConnection(DB_CONNECTION, "", "");
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    public static Map<Integer, Map<String, Object>> getItemsByIDs(List<Integer> ids) {
        Map<Integer, Map<String, Object>> results = new HashMap<>();
        getConn();

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT id, name_english, name_japanese, slots FROM item_db_re WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            selectQuery.append("?,");
        }
        selectQuery.deleteCharAt(selectQuery.length() - 1);
        selectQuery.append(")");
        PreparedStatement selectPreparedStatement = null;

        try {
            selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
            for (int i = 0; i < ids.size(); i++) {
                selectPreparedStatement.setInt(i + 1, ids.get(i));
            }
            ResultSet rs = selectPreparedStatement.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("name", rs.getString("name_japanese"));
                result.put("aegisName", rs.getString("name_english"));
                result.put("slots", rs.getInt("slots"));
                results.put(rs.getInt("id"), result);
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        } finally {
            close(selectPreparedStatement);
        }

        return results;
    }

    public static List<Integer> getMobIDsByName(String name) {
        List<Integer> results = new ArrayList<>();
        getConn();

        PreparedStatement selectPreparedStatement = null;

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT ID FROM mob_db_re WHERE LOWER(kName) LIKE ? OR LOWER(iName) LIKE ?");

        try {
            selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
            selectPreparedStatement.setString(1, String.format("%%%s%%", name.toLowerCase()));
            selectPreparedStatement.setString(2, String.format("%%%s%%", name.toLowerCase()));

            ResultSet rs = selectPreparedStatement.executeQuery();
            while (rs.next()) {
                results.add(rs.getInt("ID"));
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        } finally {
            close(selectPreparedStatement);
        }

        return results;
    }

    public static List<Map<String, Object>> getEvents() {
        List<Map<String, Object>> results = new ArrayList<>();
        getConn();

        StringBuilder selectQuery = new StringBuilder();
        PreparedStatement selectPreparedStatement = null;

        selectQuery.append("SELECT id, name, schedule, start, end, duration FROM event_db WHERE start < NOW() AND (end > NOW() OR end IS NULL)");

        try {
            selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
            ResultSet rs = selectPreparedStatement.executeQuery();
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("name", rs.getString("name"));
                result.put("schedule", rs.getString("schedule"));
                result.put("start", rs.getTimestamp("start"));
                result.put("end", rs.getTimestamp("end"));
                result.put("duration", rs.getLong("duration"));
                results.add(result);
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        } finally {
            close(selectPreparedStatement);
        }

        return results;
    }

    private static void close(PreparedStatement p) {
        try {
            if (p != null) {
                p.close();
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        }
    }

    private DataUtils() {
    }

}

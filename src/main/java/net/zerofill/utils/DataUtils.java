package net.zerofill.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

    public static final String DB_DRIVER = "org.h2.Driver";
    public static final String DB_CONNECTION = "jdbc:h2:./bifrost;MODE=MySQL";
    private static final List<String> sqlFiles = new ArrayList<>(Arrays.asList(
            "data/event_db.sql",
            "data/bifrost_db.sql"
    ));

    private static Connection conn = null;

    public static void loadAll() {
        try {
            List<URL> urls = new ArrayList<>();
            urls.add(new URL(ConfigUtils.get("rathena.db.weapon")));
            urls.add(new URL(ConfigUtils.get("rathena.db.mob")));

            for (URL url : urls) {
                try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
                    load(in);
                } catch (IOException ioe) {
                    logger.debug(ioe.getLocalizedMessage());
                }
            }
        } catch (MalformedURLException mue) {
            logger.debug(mue.getLocalizedMessage());
        }


        for (String sqlFile : sqlFiles) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(sqlFile);
            load(resourceAsStream);
        }
    }

    public static void load(InputStream file) {
        getConn();
        if (file != null) {
            try (InputStreamReader isr = new InputStreamReader(file)) {
                try (BufferedReader br = new BufferedReader(isr)) {
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (!line.startsWith("#")) {
                            builder.append(line
                                    .replaceAll("\\\\'", "''")
                                    .replaceAll("(tiny|small|medium)int", "int")
                                    .replaceAll("atk:matk", "atkmatk")
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
                    file.close();
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

    private static Connection getConn() {
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
        return conn;
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

        try (PreparedStatement selectPreparedStatement = conn.prepareStatement(selectQuery.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                selectPreparedStatement.setInt(i + 1, ids.get(i));
            }
            try (ResultSet rs = selectPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", rs.getInt("id"));
                    result.put("name", rs.getString("name_japanese"));
                    result.put("aegisName", rs.getString("name_english"));
                    result.put("slots", rs.getInt("slots"));
                    results.put(rs.getInt("id"), result);
                }
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        }

        return results;
    }

    public static List<Integer> getMobIDsByName(String name) {
        List<Integer> results = new ArrayList<>();
        getConn();

        StringBuilder selectQuery = new StringBuilder();
        selectQuery.append("SELECT ID FROM mob_db_re WHERE LOWER(kName) LIKE ? OR LOWER(iName) LIKE ?");

        try (PreparedStatement selectPreparedStatement = conn.prepareStatement(selectQuery.toString())) {
            selectPreparedStatement.setString(1, String.format("%%%s%%", name.toLowerCase()));
            selectPreparedStatement.setString(2, String.format("%%%s%%", name.toLowerCase()));

            try (ResultSet rs = selectPreparedStatement.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getInt("ID"));
                }
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        }

        return results;
    }

    public static List<Map<String, Object>> getEvents() {
        return getEvents(null);
    }

    public static List<Map<String, Object>> getEvents(String eventId) {
        List<Map<String, Object>> results = new ArrayList<>();
        getConn();

        StringBuilder selectQuery = new StringBuilder();

        selectQuery.append("SELECT id, name, schedule, start, end, duration FROM event_db WHERE (start < NOW() AND (end > NOW() OR end IS NULL))");

        if (eventId != null) {
            selectQuery.append(" AND id = ?");
        }

        try (PreparedStatement selectPreparedStatement = conn.prepareStatement(selectQuery.toString())) {
            if (eventId != null) {
                selectPreparedStatement.setString(1, eventId);
            }
            try (ResultSet rs = selectPreparedStatement.executeQuery()) {
                results = parseResults(rs);
            }
        } catch (SQLException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getLocalizedMessage());
            }
        }

        return results;
    }

    public static List<Map<String, Object>> getWeapons(Integer... args) {
        List<Map<String, Object>> results = new ArrayList<>();
        getConn();

        StringBuilder selectQuery = new StringBuilder();
        PreparedStatement selectPreparedStatement = null;

        selectQuery.append("SELECT id, name_japanese, weapon_level FROM items_db_re WHERE item_type = 5");

        try {
            if (args != null && args.length > 0) {
                selectQuery.append(" AND id IN (?)");
                Array array = conn.createArrayOf("SMALLINT", args);
                selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
                selectPreparedStatement.setArray(1, array);
            } else {
                selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
            }
            try (ResultSet rs = selectPreparedStatement.executeQuery()) {
                results = parseResults(rs);
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

    public static List<Map<String, Object>> parseResults(ResultSet resultSet) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();

        while (resultSet.next()) {
            Map<String, Object> result = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String name = metaData.getColumnName(i);
                result.put(name.toLowerCase(), resultSet.getObject(name));
            }
            results.add(result);
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

package org.midgardarmy.utils;

import org.h2.tools.RunScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataUtils.class);

    private static final String DB_DRIVER = "org.h2.Driver";
    private static final String DB_CONNECTION = "jdbc:h2:mem:test;MODE=MySQL";

    private static Connection conn = null;

    public static void load() {
        getConn();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("item_db_re.sql");

        if (resourceAsStream != null) {
            try (InputStreamReader isr = new InputStreamReader(resourceAsStream)) {
                RunScript.execute(conn, isr);
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
        PreparedStatement selectPreparedStatement = null;

        String idsString = ids.toString().substring(1, ids.toString().length() - 1);
        selectQuery.append(String.format("SELECT id, name_english, name_japanese, slots FROM item_db_re WHERE id IN (%s)", idsString));

        try {
            selectPreparedStatement = conn.prepareStatement(selectQuery.toString());
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
            try {
                if (selectPreparedStatement != null) {
                    selectPreparedStatement.close();
                }
            } catch (SQLException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getLocalizedMessage());
                }
            }
        }

        return results;
    }

    private DataUtils() {}

}

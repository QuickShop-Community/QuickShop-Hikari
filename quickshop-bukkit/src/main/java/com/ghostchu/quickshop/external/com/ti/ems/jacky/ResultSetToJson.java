package com.ghostchu.quickshop.external.com.ti.ems.jacky;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author jackypan1989 (<a href="https://jackypan1989.wordpress.com/2012/07/18/java-%E5%AD%B8%E7%BF%92%E7%AD%86%E8%A8%98-convert-resultset-to-json/">...</a>)
 */
public class ResultSetToJson {
    private ResultSetToJson() {
    }

    @NotNull
    public static JsonArray resultSetToJsonArray(@NotNull ResultSet rs) {
        JsonArray ja = new JsonArray();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                JsonObject element = new JsonObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    String columnValue = rs.getString(columnName);
                    element.addProperty(columnName, columnValue);
                }
                ja.add(element);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ja;
    }

    public static String resultSetToJsonString(ResultSet rs) {
        return resultSetToJsonObject(rs).toString();
    }

    @NotNull
    public static JsonObject resultSetToJsonObject(@NotNull ResultSet rs) {
        JsonArray ja = new JsonArray();
        JsonObject jo = new JsonObject();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                JsonObject element = new JsonObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    String columnValue = rs.getString(columnName);
                    element.addProperty(columnName, columnValue);
                }
                ja.add(element);
            }
            jo.add("result", ja);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jo;
    }

}

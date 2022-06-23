package com.ghostchu.quickshop.external.com.ti.ems.jacky;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author jackypan1989 (<a href="https://jackypan1989.wordpress.com/2012/07/18/java-%E5%AD%B8%E7%BF%92%E7%AD%86%E8%A8%98-convert-resultset-to-json/">...</a>)
 */
public class ResultSetToJson {
    public static JsonArray resultSetToJsonArray(ResultSet rs) {
        JsonObject element;
        JsonArray ja = new JsonArray();
        ResultSetMetaData rsmd;
        String columnName, columnValue;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                element = new JsonObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i + 1);
                    columnValue = rs.getString(columnName);
                    element.addProperty(columnName, columnValue);
                }
                ja.add(element);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ja;
    }

    public static JsonObject resultSetToJsonObject(ResultSet rs) {
        JsonObject element;
        JsonArray ja = new JsonArray();
        JsonObject jo = new JsonObject();
        ResultSetMetaData rsmd;
        String columnName, columnValue;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                element = new JsonObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i + 1);
                    columnValue = rs.getString(columnName);
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

    public static String resultSetToJsonString(ResultSet rs) {
        return resultSetToJsonObject(rs).toString();
    }

}

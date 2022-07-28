package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DatabaseInfoItem implements SubPasteItem {
    @Override
    public @NotNull String genBody() {
        return buildContent();
    }

    @Override
    public @NotNull String getTitle() {
        return "Database";
    }

    @NotNull
    private String buildContent() {
        try (Connection conn = QuickShop.getInstance().getSqlManager().getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            HTMLTable table = new HTMLTable(2, true);
            table.insert("Product", meta.getDatabaseProductName());
            table.insert("Version", meta.getDatabaseProductVersion());
            table.insert("Driver", meta.getDriverName());
            table.insert("Driver Version", meta.getDriverVersion());
            if (Util.parsePackageProperly("generateDatabaseFullReport").asBoolean()) {
                processFullReportGenerate(meta, table);
            }
            return table.render();
        } catch (SQLException exception) {
            return "<p>Failed to connect to database or getting metadata.</p>";
        }
    }

    private void processFullReportGenerate(@NotNull DatabaseMetaData meta, @NotNull HTMLTable table) {
        List<Class<?>> allowedClasses = Arrays.asList(
                String.class,
                Boolean.class,
                Long.class,
                Integer.class,
                Short.class,
                Float.class,
                Double.class,
                Byte.class,
                Character.class
        );
        for (Method method : meta.getClass().getDeclaredMethods()) {
            if (method.canAccess(meta)) {
                if (allowedClasses.contains(method.getReturnType())) {
                    try {
                        Object value = method.invoke(meta);
                        if (value != null) {
                            table.insert("[verbose] " + method.getName(), value.toString());
                        } else {
                            table.insert("[verbose] " + method.getName(), "null");
                        }
                    } catch (Exception exception) {
                        //ignore
                        table.insert("[verbose] " + method.getName(), exception.getMessage());
                    }
                }
            }
        }

    }
}

package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.util.PackageUtil;
import com.ghostchu.quickshop.util.paste.util.HTMLTable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DatabaseInfoItem implements SubPasteItem {

  private static final String VERBOSE_PREFIX = "[verbose] ";

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

    try(final Connection conn = QuickShop.getInstance().getSqlManager().getConnection()) {
      final DatabaseMetaData meta = conn.getMetaData();
      final HTMLTable table = new HTMLTable(2, true);
      table.insert("Product", meta.getDatabaseProductName());
      table.insert("Version", meta.getDatabaseProductVersion());
      table.insert("Driver", meta.getDriverName());
      table.insert("Driver Version", meta.getDriverVersion());
      if(PackageUtil.parsePackageProperly("generateDatabaseFullReport").asBoolean()) {
        processFullReportGenerate(meta, table);
      }
      return table.render();
    } catch(final SQLException exception) {
      return "<p>Failed to connect to database or getting metadata.</p>";
    }
  }

  private void processFullReportGenerate(@NotNull final DatabaseMetaData meta, @NotNull final HTMLTable table) {

    final List<Class<?>> allowedClasses = Arrays.asList(
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
    for(final Method method : meta.getClass().getDeclaredMethods()) {
      if(method.canAccess(meta)) {
        if(allowedClasses.contains(method.getReturnType())) {
          try {
            final Object value = method.invoke(meta);
            if(value != null) {
              table.insert(VERBOSE_PREFIX + method.getName(), value.toString());
            } else {
              table.insert(VERBOSE_PREFIX + method.getName(), "null");
            }
          } catch(final Exception exception) {
            //ignore
            table.insert(VERBOSE_PREFIX + method.getName(), exception.getMessage());
          }
        }
      }
    }

  }
}

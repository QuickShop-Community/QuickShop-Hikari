package com.ghostchu.quickshop.util.paste.util;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class HTMLTable {
    private final int columns;
    private String[] title;
    private final List<String[]> data = new LinkedList<>();

    private final boolean firstColumnBold;

    private static final String TEMPLATE = """           
            <table style="table-layout: fixed;">
              {col}
              {thead}
              {tbody}
            </table>
            """;

    /**
     * Creates an HTML Table
     *
     * @param maxColumns Table max columns, any more columns will be ignored
     */
    public HTMLTable(int maxColumns) {
        this.columns = maxColumns;
        this.firstColumnBold = false;
    }

    /**
     * Creates an HTML Table
     *
     * @param maxColumns      Table max columns, any more columns will be ignored
     * @param firstColumnBold Whether the first column should be bolded
     */
    public HTMLTable(int maxColumns, boolean firstColumnBold) {
        this.columns = maxColumns;
        this.firstColumnBold = firstColumnBold;
    }

    /**
     * Sets the table title
     *
     * @param title The title, null to clear if already set.
     */
    public void setTableTitle(@Nullable String... title) {
        this.title = title;
    }

    /**
     * Insert new row to the table
     *
     * @param data The data to insert, any more columns which overflowed than maxColumns will be ignored
     */
    public void insert(@NotNull String... data) {
        String[] f = new String[columns];
        Arrays.fill(f, "");
        System.arraycopy(data, 0, f, 0, Math.min(data.length, columns));
        if (firstColumnBold) {
            if (!StringUtils.isEmpty(f[0])) {
                f[0] = "<b>" + f[0] + "</b>";
            }
        }
        this.data.add(f);
    }

    /**
     * Render this table to HTML sources
     *
     * @return The HTML sources
     */
    @NotNull
    public String render() {
        String thead = renderHead();
        String tbody = renderBody();
        return TEMPLATE
                .replace("{col}", renderColAttributes())
                .replace("{thead}", thead)
                .replace("{tbody}", tbody);
    }

    private String renderBody() {
        StringBuilder tdBuilder = new StringBuilder();
        for (String[] line : data) {
            tdBuilder.append("<tr>");
            for (String record : line) {
                tdBuilder.append("<td>").append(record).append("</td>");
            }
            tdBuilder.append("</tr>");
        }
        return "<tbody>" + tdBuilder + "</tbody>";
    }

    @NotNull
    private String renderHead() {
        StringBuilder tdBuilder = new StringBuilder();
        if (title == null || title.length == 0) {
            return "";
        }
        for (String title : title) {
            tdBuilder.append("<th>").append(title).append("</th>");
        }
        return """
                <thead>
                <tr>
                 {th}
                </tr>
                </thead>
                """.replace("{th}", tdBuilder.toString());
    }

    private String renderColAttributes() {
        return String.format("<col style\"width: %s%%;\">\n", 100 / columns).repeat(columns);
    }
}

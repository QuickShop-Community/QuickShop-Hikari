package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
@Data
public class HeaderItem implements PasteItem {
    private final long timestamp;
    private final Map<String, String> items;
    private static final String TEMPLATE = """
            <h1>{title}</h1>
            <blockquote>
            <p>
            <b>Warning!</b><br />
            Don't send paste to public channel or anyone unless you trust them.
            </p>
            </blockquote>
            <table>
                <tbody>
                   {content}
                </tbody>
            </table>
            """;

    @NotNull
    private String buildContent() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            builder.append("<tr>");
            builder.append("<td>").append(entry.getKey()).append("</td>");
            builder.append("<td>").append(entry.getValue()).append("</td>");
            builder.append("</tr>");
        }
        return builder.toString();
    }

    @Override
    public @NotNull String toHTML() {
        return TEMPLATE
                .replace("{title}", "QuickShop-" + QuickShop.getFork() + " // Paste")
                .replace("{content}", buildContent());
    }
}

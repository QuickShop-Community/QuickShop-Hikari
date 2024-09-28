package com.ghostchu.quickshop.util.paste.item;

import com.ghostchu.quickshop.QuickShop;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Data
public class HeaderItem implements PasteItem {

  private static final String TEMPLATE = """
                                         <h1>{title}</h1>
                                         {donation-header}
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
  private static final String DONATED_HEADER = """
                                               <blockquote style="border-left-color: deeppink; background-color: rgb(253, 209, 216);">
                                                 <p>
                                                 <b>❤️ Thank you for donating</b><br />
                                                 The donor identity of this QuickShop-Hikari installation is bound to <a href="{url}"><b>{username}</b></a>.<br>
                                               Your generous support is vital to us and allows us to continue to invest in the development of QuickShop-Hikari and keep the updates free to all.
                                                 </p>
                                               </blockquote>
                                               """;
  private final long timestamp;
  private final Map<String, String> items;

  public HeaderItem(long timestamp, Map<String, String> items) {

    this.timestamp = timestamp;
    this.items = items;
  }

  @Override
  public @NotNull String toHTML() {

    String base = TEMPLATE
            .replace("{title}", "QuickShop-" + QuickShop.getInstance().getFork() + " // Paste (" + QuickShop.getInstance().getVersion() + ")")
            .replace("{content}", buildContent());
    base = base.replace("{donation-header}", "");
    return base;
  }

  @NotNull
  private String buildContent() {

    StringBuilder builder = new StringBuilder();
    for(Map.Entry<String, String> entry : items.entrySet()) {
      builder.append("<tr>");
      builder.append("<td>").append(entry.getKey()).append("</td>");
      builder.append("<td>").append(entry.getValue()).append("</td>");
      builder.append("</tr>");
    }
    return builder.toString();
  }
}

package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


@Getter
@Setter
/*
 A utils for print sheet on chat.
*/
public class ChatSheetPrinter {

  private final CommandSender p;

  public ChatSheetPrinter(final CommandSender p) {

    this.p = p;
  }

  public ChatSheetPrinter(final QUser p) {

    this.p = p.getBukkitPlayer().orElse(null);
  }

  public void printCenterLine(@NotNull final Component text) {

    if(Util.isEmptyComponent(text)) {
      return;
    }

    MsgUtil.sendDirectMessage(p,
                              QuickShop.getInstance().text().of(p, "tableformat.left_half_line").forLocale()
                                      .append(text)
                                      .append(QuickShop.getInstance().text().of(p, "tableformat.right_half_line").forLocale()));
  }

  public void printFooter() {

    printFullLine();
  }

  private void printFullLine() {

    MsgUtil.sendDirectMessage(p, QuickShop.getInstance().text().of(p, "tableformat.full_line").forLocale());
  }

  public void printHeader() {

    printFullLine();
  }

  public void printLine(@NotNull final Component component) {

    if(Util.isEmptyComponent(component)) {
      return;
    }
    MsgUtil.sendDirectMessage(p, QuickShop.getInstance().text().of(p, "tableformat.left_begin").forLocale().append(component));
  }

  public CommandSender getSender() {

    return p;
  }
}

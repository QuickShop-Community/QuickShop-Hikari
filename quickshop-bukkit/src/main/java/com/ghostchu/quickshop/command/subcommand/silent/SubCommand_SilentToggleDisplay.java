package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_SilentToggleDisplay extends SubCommand_SilentBase {

  public SubCommand_SilentToggleDisplay(final QuickShop plugin) {

    super(plugin);
  }

  @Override
  protected void doSilentCommand(final Player sender, @NotNull final Shop shop, @NotNull final CommandParser parser) {

    shop.setDisableDisplay(!shop.isDisableDisplay());
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
    Util.playSound(sender, "effect.sound.shop.toggle-display");
    Util.playParticle(sender, shop.bukkitLocation(), "effect.particle.shop.toggle-display");
    MsgUtil.sendControlPanelInfo(sender, shop);
  }

}

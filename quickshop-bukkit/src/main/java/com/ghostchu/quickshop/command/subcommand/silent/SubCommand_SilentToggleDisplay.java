package com.ghostchu.quickshop.command.subcommand.silent;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.util.MsgUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_SilentToggleDisplay extends SubCommand_SilentBase {

    public SubCommand_SilentToggleDisplay(QuickShop plugin) {
        super(plugin);
    }

    @Override
    protected void doSilentCommand(Player sender, @NotNull Shop shop, @NotNull CommandParser parser) {
        shop.setDisableDisplay(!shop.isDisableDisplay());
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
        MsgUtil.sendControlPanelInfo(sender, shop);
    }

}

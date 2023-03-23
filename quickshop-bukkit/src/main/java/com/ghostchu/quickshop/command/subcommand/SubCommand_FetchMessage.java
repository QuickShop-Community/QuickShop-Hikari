package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SubCommand_FetchMessage implements CommandHandler<Player> {

    public SubCommand_FetchMessage() {
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        Util.mainThreadRun(() -> MsgUtil.flush(sender));
    }


}

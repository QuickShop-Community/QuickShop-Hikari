package org.maxgamer.quickshop.platform.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.platform.Platform;

public class PaperPlatform implements Platform {
    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        sign.line(line, component);
    }

    @Override
    public @NotNull TranslatableComponent getItemTranslationKey(@NotNull Material material) {
        try {
            return Component.translatable(material.getTranslationKey());
        } catch (Error e) {
            return Component.translatable(material.translationKey());
        }
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        Bukkit.getCommandMap().register(prefix,command);
        Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    @Override
    public boolean isServerStopping() {
        return Bukkit.isStopping();
    }
}

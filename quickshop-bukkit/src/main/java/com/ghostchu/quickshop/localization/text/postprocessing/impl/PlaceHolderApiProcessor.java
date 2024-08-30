package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class PlaceHolderApiProcessor implements PostProcessor {
    @Override
    public @NotNull Component process(@NotNull Component text, @Nullable CommandSender sender, Component... args) {
        /*if (sender instanceof OfflinePlayer offlinePlayer) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
                String json = GsonComponentSerializer.gson().serialize(text);
                json = PlaceholderAPI.setPlaceholders(offlinePlayer, json);
                return GsonComponentSerializer.gson().deserialize(json);
            }
        }*/
        return text;
    }

}

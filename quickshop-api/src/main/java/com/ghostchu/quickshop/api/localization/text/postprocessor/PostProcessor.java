package com.ghostchu.quickshop.api.localization.text.postprocessor;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Post-processing the Components
 */
public interface PostProcessor {
    /**
     * Process the string
     *
     * @param text   Original string
     * @param sender The command sender
     * @param args   The arguments
     * @return The string that processed
     */
    @NotNull
    Component process(@NotNull Component text, @Nullable CommandSender sender, @Nullable Component... args);
}

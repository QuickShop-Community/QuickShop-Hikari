package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForceReplaceFillerProcessor implements PostProcessor {
    @Override
    public @NotNull Component process(@NotNull Component text, @Nullable CommandSender sender, Component... args) {
        String json = GsonComponentSerializer.gson().serialize(text);
        for (Component arg : args) {
            try {
                json = MsgUtil.fillArgs(json, PlainTextComponentSerializer.plainText().serialize(arg));
            } catch (Exception e) {
                Log.debug("Failed to fill args: " + e.getMessage());
            }
        }
        return GsonComponentSerializer.gson().deserialize(json);
    }
}

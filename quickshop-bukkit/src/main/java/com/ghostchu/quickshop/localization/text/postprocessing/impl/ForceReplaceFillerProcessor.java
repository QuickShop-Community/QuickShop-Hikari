package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class ForceReplaceFillerProcessor implements PostProcessor {
    @Override
    public @NotNull Component process(@NotNull Component text, @Nullable CommandSender sender, Component... args) {
        String json = GsonComponentSerializer.gson().serialize(text);
        String[] plainArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            plainArgs[i] = PlainTextComponentSerializer.plainText().serialize(args[i]);
        }
        try {
            json = MsgUtil.fillArgs(json, plainArgs);
        } catch (Exception e) {
            Log.debug("Failed to fill args: " + e.getMessage());
        }
        return GsonComponentSerializer.gson().deserialize(json);
    }
}

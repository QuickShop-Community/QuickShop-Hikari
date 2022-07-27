package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FillerProcessor implements PostProcessor {
    @Override
    public @NotNull Component process(@NotNull Component text, @Nullable CommandSender sender, Component... args) {
        return MsgUtil.fillArgs(text, args);
    }
}

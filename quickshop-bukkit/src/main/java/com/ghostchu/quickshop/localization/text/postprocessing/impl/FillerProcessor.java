package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode
public class FillerProcessor implements PostProcessor {

  @Override
  public @NotNull Component process(@NotNull final Component text, @Nullable final CommandSender sender, final Component... args) {

    return MsgUtil.fillArgs(text, args);
  }
}

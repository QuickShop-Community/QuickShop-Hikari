package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import com.ghostchu.quickshop.util.MsgUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FillerProcessor implements PostProcessor {

  @Override
  @NotNull
  public Component process(@NotNull final Component text, @Nullable final CommandSender sender, final Component... args) {

    return MsgUtil.fillArgs(text, args);
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof FillerProcessor)) return false;
    final FillerProcessor other = (FillerProcessor)o;
    return true;
  }

  @Override
  public int hashCode() {

    return Objects.hash();
  }
}

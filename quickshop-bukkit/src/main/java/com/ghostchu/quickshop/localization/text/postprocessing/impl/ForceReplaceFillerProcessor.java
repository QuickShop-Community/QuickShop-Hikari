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

import java.util.Objects;

public class ForceReplaceFillerProcessor implements PostProcessor {

  @Override
  @NotNull
  public Component process(@NotNull final Component text, @Nullable final CommandSender sender, final Component... args) {

    String json = GsonComponentSerializer.gson().serialize(text);
    final String[] plainArgs = new String[args.length];
    for(int i = 0; i < args.length; i++) {
      plainArgs[i] = PlainTextComponentSerializer.plainText().serialize(args[i]);
    }
    try {
      json = MsgUtil.fillArgs(json, plainArgs);
    } catch(Exception e) {
      Log.debug("Failed to fill args: " + e.getMessage());
    }
    return GsonComponentSerializer.gson().deserialize(json);
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof ForceReplaceFillerProcessor)) return false;
    final ForceReplaceFillerProcessor other = (ForceReplaceFillerProcessor)o;
    return true;
  }

  @Override
  public int hashCode() {

    return Objects.hash();
  }
}

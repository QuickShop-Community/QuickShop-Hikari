package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PlaceHolderApiProcessor implements PostProcessor {

  @Override
  @NotNull
  public Component process(@NotNull final Component text, @Nullable final CommandSender sender, final Component... args) {
        /*if (sender instanceof OfflinePlayer offlinePlayer) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceHolderAPI")) {
                String json = GsonComponentSerializer.gson().serialize(text);
                json = PlaceholderAPI.setPlaceholders(offlinePlayer, json);
                return GsonComponentSerializer.gson().deserialize(json);
            }
        }*/
    return text;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof PlaceHolderApiProcessor)) return false;
    final PlaceHolderApiProcessor other = (PlaceHolderApiProcessor)o;
    return true;
  }

  @Override
  public int hashCode() {

    return Objects.hash();
  }
}

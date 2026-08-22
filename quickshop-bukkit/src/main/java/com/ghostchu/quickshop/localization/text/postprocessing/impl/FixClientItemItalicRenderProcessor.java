package com.ghostchu.quickshop.localization.text.postprocessing.impl;

import com.ghostchu.quickshop.api.localization.text.postprocessor.PostProcessor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/*
    This processor fixed the component on Item will render as ITALICS style on client.
    https://github.com/KyoriPowered/adventure/issues/534
 */
public class FixClientItemItalicRenderProcessor implements PostProcessor {

  @Override
  @NotNull
  public Component process(@NotNull Component text, @Nullable final CommandSender sender, final Component... args) {

    if(!text.hasDecoration(TextDecoration.ITALIC)) {
      text = text.decoration(TextDecoration.ITALIC, false);
    }
    return text;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof FixClientItemItalicRenderProcessor)) return false;
    final FixClientItemItalicRenderProcessor other = (FixClientItemItalicRenderProcessor)o;
    return true;
  }

  @Override
  public int hashCode() {

    return Objects.hash();
  }
}

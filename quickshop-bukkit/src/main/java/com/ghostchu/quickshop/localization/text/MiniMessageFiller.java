package com.ghostchu.quickshop.localization.text;

/*
 * QuickShop-Hikari
 * Copyright (C) 2026 Daniel "creatorfromhell" Vidmar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.ghostchu.quickshop.util.logger.Log;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MiniMessageFiller
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public final class MiniMessageFiller {

  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

  private MiniMessageFiller() { }

  @NotNull
  public static String fillRaw(@NotNull final String message, @Nullable final Component... args) {
    String filled = message;

    if(args == null || args.length == 0) {
      return filled;
    }

    for(int i = 0; i < args.length; i++) {
      final String placeholder = "{" + i + "}";
      final String replacement = serializeForMiniMessage(args[i]);
      filled = filled.replace(placeholder, replacement);
    }

    return filled;
  }

  @NotNull
  public static Component fill(@NotNull String message, @NotNull final MiniMessage miniMessage, @NotNull final TagResolver[] extraResolvers, @Nullable final Component... args) {

    final List<TagResolver> resolvers = new ArrayList<>(Arrays.asList(extraResolvers));

    if (args != null) {
      for (int i = 0; i < args.length; i++) {

        message = message.replace("{" + i + "}", "<arg_" + i + ">");
        resolvers.add(Placeholder.component("arg_" + i, args[i]));
      }
    }

    return miniMessage.deserialize(message, TagResolver.resolver(resolvers));
  }

  @NotNull
  private static String serializeForMiniMessage(@Nullable final Component component) {
    if(component == null) {
      return "";
    }

    // Preserves formatting if the arg is already a styled Adventure component.
    return MINI_MESSAGE.serialize(component);
  }
}
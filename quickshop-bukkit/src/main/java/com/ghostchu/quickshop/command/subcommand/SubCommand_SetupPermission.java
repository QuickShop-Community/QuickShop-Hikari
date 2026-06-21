package com.ghostchu.quickshop.command.subcommand;

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

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.ghostchu.quickshop.command.SimpleCommandManager.MODERATOR_NODES;

/**
 * DisplayEntityItemManager
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class SubCommand_SetupPermission implements CommandHandler<CommandSender> {

  private final QuickShop plugin;
  final RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

  public SubCommand_SetupPermission(final QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if (parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "command.wrong-args").send();
      return;
    }

    if (provider == null) {
      return;
    }

    final String groupName = (parser.getArgs().size() >= 2)? parser.getArgs().get(1) : "default";
    final LuckPerms luckPerms = provider.getProvider();

    //final Group luckPermsGroup = luckPerms.getGroupManager().getGroup(group);
    //if (luckPermsGroup == null) {
    //  plugin.text().of(sender, "command.wrong-args").send();
    //  return;
    //}

    switch(parser.getArgs().getFirst().toLowerCase(Locale.ROOT)) {

      case "player" -> {

        final String permission = "quickshop.player";

        luckPerms.getGroupManager().loadGroup(groupName).thenAcceptAsync(groupOptional -> {
          if (groupOptional.isEmpty()) {
            plugin.text().of(sender, "command.permission-no-group", groupName).send();
            return;
          }

          final Group group = groupOptional.get();

          final Node node = PermissionNode.builder(permission).value(true).build();

          group.data().add(node);

          luckPerms.getGroupManager().saveGroup(group);

          plugin.text().of(sender, "command.permission-added", permission, groupName).send();
        });
      }
      case "moderator" -> {
        if (groupName.equalsIgnoreCase("default")) {
          plugin.text().of(sender, "command.permission-no-default-moderator", groupName).send();
          return;
        }

        final String permission = "quickshop.moderator";

        luckPerms.getGroupManager().loadGroup(groupName).thenAcceptAsync(groupOptional -> {
          if (groupOptional.isEmpty()) {
            plugin.text().of(sender, "command.permission-no-group", groupName).send();
            return;
          }

          final Group group = groupOptional.get();

          final Node node = PermissionNode.builder(permission).value(true).build();

          group.data().add(node);

          luckPerms.getGroupManager().saveGroup(group);

          plugin.text().of(sender, "command.permission-added", permission, groupName).send();

        });

      }
      case "check" -> {
        checkModeratorPermission(sender, luckPerms);
      }
      default->plugin.text().of(sender, "command.wrong-args").send();
    }
  }

  public static void checkModeratorPermission(final CommandSender sender, final LuckPerms luckPerms) {
    final String defaultGroup = "default";

    luckPerms.getGroupManager().loadGroup(defaultGroup).thenAcceptAsync(groupOptional -> {
      if (groupOptional.isEmpty()) {
        QuickShop.getInstance().text().of(sender, "command.permission-no-group", defaultGroup).send();
        return;
      }

      final Group group = groupOptional.get();

      final Set<String> permissionsFound = new HashSet<>();

      for (final String permissionNode : MODERATOR_NODES) {

        if (group.getNodes().stream().anyMatch(node -> node.getKey().equalsIgnoreCase(permissionNode))) {

          permissionsFound.add(permissionNode);

          final Node node = PermissionNode.builder(permissionNode).value(false).build();
          group.data().remove(node);

          luckPerms.getGroupManager().saveGroup(group);
        }
      }

      if (permissionsFound.isEmpty()) {
        QuickShop.getInstance().text().of(sender, "command.permission-check-not-found").send();
        return;
      }

      QuickShop.getInstance().text().of(sender, "command.permission-check-found", String.join(", ", permissionsFound)).send();
    });
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final String commandLabel, @NotNull final CommandParser parser) {

    if (parser.getArgs().size() == 1) {
      return List.of("player", "moderator", "check");
    }

    if (parser.getArgs().size() == 2) {

      if (provider == null || parser.getArgs().getFirst().equalsIgnoreCase("check")) {
        return Collections.emptyList();
      }

      return provider.getProvider().getGroupManager().getLoadedGroups().stream().map(Group::getName).toList();
    }
    return Collections.emptyList();
  }
}
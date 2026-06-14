package com.ghostchu.quickshop.util;

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
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EntityUtil
 *
 * @author creatorfromhell
 * @since 6.3.0.0
 */
public class EntityUtil {

  /**
   * Spawns a block display entity at the specified location using the provided material,
   * with default scale and visibility parameters. The display entity will only be visible
   * to the specified player and will despawn automatically after a predefined time.
   *
   * @param player   The player for whom the block display will be visible. All other players will have the entity hidden.
   * @param location The location where the block display should be spawned.
   * @param material The material used for the block display.
   */
  public static BlockDisplay spawnDisplayBlockFor(final @Nullable Player player, final Location location, final Material material) {

    return spawnDisplayBlockFor(player, location, material, new Vector3f(1.02f, 1.02f, 1.02f), 15, 10);
  }

  /**
   * Spawns a block display entity at a specified location with the given material and transformation properties.
   * The display entity will only be visible to the provided player and will despawn after a specified time.
   *
   * @param player     The player for whom the block display will be visible. All other players will have the entity hidden.
   * @param location   The location where the block display should be spawned.
   * @param material   The material used for the block display.
   * @param scale      The scale transformation to apply to the display, represented as a {@code Vector3f}.
   * @param viewRange  The max view range within which the block display will be visible to the player.
   * @param despawn    The time, in seconds, after which the display entity will automatically despawn.
   *                   A value of 0 means the entity will not despawn automatically.
   */
  public static BlockDisplay spawnDisplayBlockFor(final @Nullable Player player,
                                                  final Location location,
                                                  final Material material,
                                                  final Vector3f scale,
                                                  final int viewRange,
                                                  final int despawn) {

    final Location blockLoc = location.getBlock().getLocation();

    final BlockDisplay display = blockLoc.getWorld().spawn(blockLoc.clone().add(-0.01, -0.01, -0.01), BlockDisplay.class);

    display.setBlock(material.createBlockData());
    display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), scale, new AxisAngle4f()));
    display.setBrightness(new Display.Brightness(15, 15));
    display.setViewRange(viewRange);
    display.setPersistent(false);
    display.setVisibleByDefault(false);

    if (player != null) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), display);
    }

    if (despawn > 0) {
      QuickShop.folia().getScheduler().runAtLocationLater(location, () -> {

        if (display.isValid()) {
          display.remove();
        }
      }, despawn, TimeUnit.SECONDS);
    }

    return display;
  }

  /**
   * Spawns an item display at the specified location for a given player, with the provided item.
   * Configures the display with default scaling, brightness, and view distance settings.
   *
   * @param player the player for whom the item display is spawned
   * @param location the location where the item display will be spawned
   * @param itemStack the item to be displayed
   * @return the spawned ItemDisplay object
   */
  public static ItemDisplay spawnDisplayItemFor(final @Nullable Player player, final Location location, final ItemStack itemStack) {

    return spawnDisplayItemFor(player, location, itemStack, new Vector3f(1.0f, 1.0f, 1.0f), 15, 10);
  }
  /**
   * Spawns an ItemDisplay entity at the specified location, configured with the provided
   * item stack, scale, view range, and despawn time. The spawned entity is configured not
   * to be persistent or visible by default, and is explicitly shown to the provided player.
   *
   * @param player the player to whom the spawned ItemDisplay entity should be explicitly shown
   * @param location the location at which to spawn the ItemDisplay entity
   * @param itemStack the item stack to be displayed by the spawned ItemDisplay entity
   * @param scale the scale transformation to apply to the ItemDisplay entity
   * @param viewRange the view range of the ItemDisplay entity
   * @param despawn the time in seconds after which the ItemDisplay entity should despawn;
   *                if less than or equal to 0, the entity will not be automatically despawned
   * @return the spawned ItemDisplay entity
   */
  public static ItemDisplay spawnDisplayItemFor(final @Nullable Player player,
                                                final Location location,
                                                final ItemStack itemStack,
                                                final Vector3f scale,
                                                final int viewRange,
                                                final int despawn) {

    final ItemDisplay display = location.getWorld().spawn(location, ItemDisplay.class);

    display.setItemStack(itemStack);
    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
    display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), scale, new AxisAngle4f()));
    display.setBrightness(new Display.Brightness(15, 15));
    display.setViewRange(viewRange);
    display.setPersistent(false);
    display.setVisibleByDefault(false);

    if (player != null) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), display);
    }

    if (despawn > 0) {
      System.out.println("despawn");
      QuickShop.folia().getScheduler().runAtLocationLater(location, () -> {

        if (display.isValid()) {
          display.remove();
        }
      }, despawn, TimeUnit.SECONDS);
    }

    return display;
  }

  /**
   * Spawns a text display entity at the specified location with the provided content.
   * The display entity will only be visible to the specified player and will automatically
   * despawn after a predefined time. Default scale, view range, alignment, and despawn
   * parameters are applied.
   *
   * @param player   The player for whom the text display will be visible. All other players
   *                 will have the entity hidden.
   * @param location The location where the text display should be spawned.
   * @param content  The text content that will be displayed.
   * @return A {@code TextDisplay} object representing the spawned text display entity.
   */
  public static TextDisplay spawnDisplayTextFor(final @Nullable Player player, final Location location, final Component content) {

    return spawnDisplayTextFor(player, location, content, new Vector3f(1.0f, 1.0f, 1.0f), 15, TextDisplay.TextAlignment.CENTER, 10);
  }

  /**
   * Spawns a text display entity at the specified location with the provided content,
   * scale, view range, alignment, and despawn time. The display entity will only be
   * visible to the specified player, while it will remain hidden from all other players.
   * The entity can optionally despawn after a specified time.
   *
   * @param player    The player for whom the text display will be visible. All other
   *                  players will have the entity hidden.
   * @param location  The location where the text display should be spawned.
   * @param content   The text content that will be displayed.
   * @param scale     The scale transformation to apply to the display, represented as a {@code Vector3f}.
   * @param viewRange The maximum view range (in blocks) within which the text display will be
   *                  visible to the player.
   * @param alignment The alignment of the text within the display.
   * @param despawn   The time, in seconds, after which the display entity will automatically despawn.
   *                  A value of 0 means the entity will not despawn automatically.
   * @return A {@code TextDisplay} object representing the spawned text display entity.
   */
  public static TextDisplay spawnDisplayTextFor(final @Nullable Player player,
                                                final Location location,
                                                final Component content,
                                                final Vector3f scale,
                                                final int viewRange,
                                                final TextDisplay.TextAlignment alignment,
                                                final int despawn) {
    final TextDisplay text = location.getWorld().spawn(location, TextDisplay.class);

    text.text(content);
    text.setAlignment(alignment);
    text.setBillboard(Display.Billboard.CENTER);
    text.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(), scale, new AxisAngle4f()));
    text.setBrightness(new Display.Brightness(15, 15));
    text.setViewRange(viewRange);
    text.setPersistent(false);
    text.setVisibleByDefault(false);

    if (player != null) {
      player.showEntity(QuickShop.getInstance().getJavaPlugin(), text);
    }

    if (despawn > 0) {
      QuickShop.folia().getScheduler().runAtLocationLater(location, () -> {

        if (text.isValid()) {
          text.remove();
        }
      }, despawn, TimeUnit.SECONDS);
    }

    return text;
  }

  /**
   * Displays a visual trail for the player from their current location to a specified shop location.
   * The trail is composed of particles represented as BlockDisplay entities.
   * The trail automatically despawns after the given amount of time.
   *
   * @param player  The player for whom the trail will be displayed.
   * @param shopLoc The location of the shop to which the trail leads.
   * @param despawn The time in seconds after which the trail will disappear.
   */
  public static void showShopTrail(final @NotNull Player player, final Location shopLoc, final int despawn) {
    final Location start = player.getLocation().clone();
    final Location end = shopLoc.clone().add(0.5, 0.2, 0.5);

    final World world = start.getWorld();
    if (world == null || !world.equals(end.getWorld())) {
      return;
    }

    final Vector direction = end.toVector().subtract(start.toVector());
    final double distance = direction.length();

    if (distance <= 1) {
      return;
    }

    direction.normalize();

    final List<BlockDisplay> trail = new ArrayList<>();

    for (double d = 1.5; d < distance; d += 1.25) {

      final Location point = start.clone().add(direction.clone().multiply(d));

      final BlockDisplay dot = world.spawn(point, BlockDisplay.class);
      dot.setBlock(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData());
      dot.setBrightness(new Display.Brightness(15, 15));
      dot.setViewRange(32);
      dot.setPersistent(false);
      dot.setVisibleByDefault(false);

      dot.setTransformation(new Transformation(
              new Vector3f(-0.12f, -0.12f, -0.12f),
              new AxisAngle4f(),
              new Vector3f(0.24f, 0.24f, 0.24f),
              new AxisAngle4f()
      ));

      player.showEntity(QuickShop.getInstance().getJavaPlugin(), dot);

      trail.add(dot);
    }

    QuickShop.folia().getScheduler().runAtLocationLater(
            shopLoc, task -> trail.forEach(entity -> {

              if(entity != null && entity.isValid()) {

                entity.remove();
              }
            }), despawn, TimeUnit.SECONDS);
  }

  /**
   * Spawns an animated block display entity at the specified location that cycles
   * through the given array of materials. The display entity will only be visible
   * to the specified player and can optionally be set to despawn automatically.
   * The animation period specifies the time interval at which the displayed
   * material changes.
   *
   * @param player          The player for whom the block display will be visible.
   *                        All other players will have the entity hidden.
   * @param location        The location where the block display should be spawned.
   * @param materials       An array of materials through which the animation will cycle.
   * @param scale           The scale transformation to apply to the display,
   *                        represented as a {@code Vector3f}.
   * @param viewRange       The maximum view range (in blocks) within which the
   *                        block display will be visible to the player.
   * @param despawn         The time, in seconds, after which the display entity
   *                        will automatically despawn. A value of 0 means the entity
   *                        will not despawn automatically.
   * @param animationPeriod The time interval, in seconds, between each animation frame,
   *                        where each frame corresponds to a change in the displayed material.
   * @return A {@code BlockDisplay} object representing the spawned animated block display entity.
   */
  public static BlockDisplay spawnDisplayBlockAnimationFor(final @Nullable Player player,
                                                           final Location location,
                                                           final Material[] materials,
                                                           final Vector3f scale,
                                                           final int viewRange,
                                                           final int despawn,
                                                           final long animationPeriod) {

    final BlockDisplay display = spawnDisplayBlockFor(player, location, materials[0], scale, viewRange, despawn);

    final AtomicInteger index = new AtomicInteger();

    QuickShop.folia().getScheduler().runAtLocationTimer(location, scheduledTask -> {
              if (!display.isValid()) {

                scheduledTask.cancel();
                return;
              }

              final int i = index.getAndUpdate(v ->(v + 1) % materials.length);
              display.setBlock(materials[i].createBlockData());
            }, 0L, animationPeriod, TimeUnit.SECONDS);

    return display;
  }

  /**
   * Spawns a text display animation at the specified location for a player. The animation
   * cycles through an array of text components at a specified interval.
   *
   * @param player The player for whom the text display will be spawned. This defines the
   *               visibility of the text display to this specific player.
   * @param location The location where the text display will be spawned.
   * @param content An array of {@link Component} objects that represent the text content
   *                to be displayed in a cyclic animation.
   * @param scale A {@link Vector3f} representing the scale of the text display.
   * @param viewRange The maximum distance (in blocks) at which the text display can be
   *                  viewed by the player.
   * @param alignment The {@link TextDisplay.TextAlignment} defining how the text content
   *                  is aligned within the display.
   * @param despawn The duration (in ticks) after which the text display will automatically
   *                despawn. A value of -1 indicates that the display will not despawn automatically.
   * @param animationPeriod The interval (in seconds) at which the text display cycles
   *                        through the provided content array.
   * @return A {@link TextDisplay} object representing the spawned text display, allowing
   *         further interaction or modifications to the display.
   */
  public static TextDisplay spawnDisplayTextAnimationFor(final @Nullable Player player,
                                                         final Location location,
                                                         final Component[] content,
                                                         final Vector3f scale,
                                                         final int viewRange,
                                                         final TextDisplay.TextAlignment alignment,
                                                         final int despawn,
                                                         final long animationPeriod) {

    final TextDisplay text = spawnDisplayTextFor(player, location, content[0], scale, viewRange, alignment, despawn);

    final AtomicInteger index = new AtomicInteger();
    QuickShop.folia().getScheduler().runAtLocationTimer(location, scheduledTask -> {
      if (!text.isValid()) {

        scheduledTask.cancel();
        return;
      }

      final int i = index.getAndUpdate(v ->(v + 1) % content.length);
      text.text(content[i]);
    }, 0L, animationPeriod, TimeUnit.SECONDS);

    return text;
  }
}
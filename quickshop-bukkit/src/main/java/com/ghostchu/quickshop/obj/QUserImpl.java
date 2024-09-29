package com.ghostchu.quickshop.obj;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public final class QUserImpl implements QUser {

  @JsonUtil.Hidden
  private final PlayerFinder finder;
  @JsonUtil.Hidden
  private final ExecutorService executorService;
  private String username;
  private UUID uniqueId;
  private boolean realPlayer;

  private QUserImpl(final PlayerFinder finder, final String string, final ExecutorService executorService) {

    this.finder = finder;
    this.executorService = executorService;
    parseString(string);
  }

  private QUserImpl(final PlayerFinder finder, final UUID uuid, final ExecutorService executorService) {

    this.finder = finder;
    this.executorService = executorService;
    parseString(uuid.toString());
  }

  private QUserImpl(final UUID uuid, final String username, final boolean realPlayer) {

    this.finder = null;
    this.uniqueId = uuid;
    this.username = username;
    this.realPlayer = realPlayer;
    this.executorService = QuickExecutor.getPrimaryProfileIoExecutor();
  }

  private void parseString(final String string) {

    if(CommonUtil.isUUID(string)) {
      parseFromUUID(string);
    } else {
      parseFromUsername(string);
    }
    endCheck();
  }

  private void parseFromUsername(final String string) {

    if(isBracketedString(string)) {
      parseFromUsernameFromVirtualPlayer(string);
    } else {
      parseFromUsernameFromRealPlayer(string);
    }

  }

  private void parseFromUsernameFromRealPlayer(final String string) {

    this.realPlayer = true;
    this.username = string;
    this.uniqueId = this.finder.name2Uuid(username, true, executorService);
    if(this.uniqueId == null) {
      throw new IllegalArgumentException("Cannot find uuid from username:" + username);
    }
  }

  private void parseFromUsernameFromVirtualPlayer(final String string) {

    final String unbracketedString = removeBrackets(string);
    this.realPlayer = false;
    this.uniqueId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + unbracketedString).getBytes(StandardCharsets.UTF_8));
    this.username = unbracketedString;
  }

  private void parseFromUUID(final String string) {

    this.realPlayer = true;
    this.uniqueId = UUID.fromString(string);
    this.finder.uuid2NameFuture(this.uniqueId, true, executorService)
            .thenAccept(result->{
              this.username = result;
              endCheck();
            })
            .exceptionally(throwable->{
              Log.debug(Level.WARNING, "Failed to get username from uuid:" + throwable.getMessage());
              return null;
            });
  }

  private void endCheck() {

    if(this.username != null && CommonUtil.isUUID(this.username)) {
      QuickShop.getInstance().logger().warn("Warning! The username of QUser is a uuid! This may cause some problems!", new IllegalStateException("The username of QUser is a uuid!"));
    }
  }

  private boolean isBracketedString(final String input) {

    return input.startsWith("[") && input.endsWith("]");
  }

  private String removeBrackets(final String input) {

    if(isBracketedString(input)) {
      return input.substring(1, input.length() - 1);
    }
    return input;
  }

  @Override
  public @Nullable String getUsername() {

    return this.username;
  }

  @Override
  public @NotNull Optional<String> getUsernameOptional() {

    return Optional.ofNullable(this.username);
  }

  @Override
  public @NotNull String getDisplay() {

    if(this.username != null) {
      if(isRealPlayer()) {
        return this.username;
      } else {
        return "[" + this.username + "]";
      }
    }
    return this.uniqueId.toString();
  }

  @Override
  public boolean isFull() {

    if(isRealPlayer()) {
      return this.username != null && this.uniqueId != null;
    }
    return this.username != null;
  }

  @Override
  public @Nullable UUID getUniqueId() {

    return this.uniqueId;
  }

  @Override
  public @NotNull Optional<UUID> getUniqueIdOptional() {

    return Optional.ofNullable(this.uniqueId);
  }

  @Override
  public boolean isRealPlayer() {

    return this.realPlayer && this.uniqueId != null;
  }

  @Override
  public Optional<UUID> getUniqueIdIfRealPlayer() {

    if(isRealPlayer()) {
      return Optional.of(this.uniqueId);
    }
    return Optional.empty();
  }

  @Override
  @NotNull
  public Optional<String> getUsernameIfRealPlayer() {

    if(isRealPlayer()) {
      return Optional.of(this.username);
    }
    return Optional.empty();
  }

  @Override
  public void setUsername(final String username) {

    this.username = username;
  }

  @Override
  public void setUniqueId(final UUID uuid) {

    this.uniqueId = uuid;
  }

  @Override
  public void setRealPlayer(final boolean isRealPlayer) {

    if(this.uniqueId == null) {
      throw new IllegalStateException("Before set a QUser to realplayer, the uniqueId must be filled");
    }
    this.realPlayer = isRealPlayer;
  }

  @Override
  public String serialize() {

    final String serialized;
    if(this.realPlayer) {
      if(this.uniqueId != null) {
        serialized = this.uniqueId.toString();
      } else {
        serialized = this.username;
      }
    } else {
      serialized = "[" + this.username + "]";
    }
    return serialized;
  }

  public static QUserImpl deserialize(final PlayerFinder finder, final String serialized, final ExecutorService executorService) {

    return new QUserImpl(finder, serialized, executorService);
  }

  public void set(final String string) {

    parseString(string);
  }

  @Override
  public Optional<Player> getBukkitPlayer() {

    if(isRealPlayer()) {
      if(this.uniqueId != null) {
        return Optional.ofNullable(Bukkit.getPlayer(this.uniqueId));
      }
      if(this.username != null) {
        return Optional.ofNullable(Bukkit.getPlayer(this.username));
      }
    }
    return Optional.empty();
  }

  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final String string) {

    return CompletableFuture.supplyAsync(()->new QUserImpl(finder, string, QuickExecutor.getPrimaryProfileIoExecutor()), QuickExecutor.getCommonExecutor());
  }

  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final UUID uuid) {

    return CompletableFuture.supplyAsync(()->new QUserImpl(finder, uuid, QuickExecutor.getPrimaryProfileIoExecutor()), QuickExecutor.getCommonExecutor());
  }

  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final String string, @NotNull final ExecutorService executorService) {

    return CompletableFuture.supplyAsync(()->new QUserImpl(finder, string, executorService), QuickExecutor.getCommonExecutor());
  }

  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final UUID uuid, @NotNull final ExecutorService executorService) {

    return CompletableFuture.supplyAsync(()->new QUserImpl(finder, uuid, executorService), QuickExecutor.getCommonExecutor());
  }

  public static QUser createFullFilled(final UUID uuid, final String username, final boolean realPlayer) {

    return new QUserImpl(uuid, username, realPlayer);
  }

  public static QUser createFullFilled(final Player player) {

    return new QUserImpl(player.getUniqueId(), player.getName(), true);
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final String string, @NotNull final ExecutorService executorService) {

    return new QUserImpl(finder, string, executorService);
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final UUID uuid, @NotNull final ExecutorService executorService) {

    return new QUserImpl(finder, uuid, executorService);
    //return QUSER_CACHE.get(uuid, () -> new QUserImpl(finder, uuid));
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final String string) {

    return new QUserImpl(finder, string, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final UUID uuid) {

    return new QUserImpl(finder, uuid, QuickExecutor.getPrimaryProfileIoExecutor());
    //return QUSER_CACHE.get(uuid, () -> new QUserImpl(finder, uuid));
  }

  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final CommandSender sender, final ExecutorService executorService) {

    if(sender instanceof Player player) {
      return CompletableFuture.supplyAsync(()->createFullFilled(player));
    }
    if(sender instanceof OfflinePlayer offlinePlayer) {
      return createAsync(finder, offlinePlayer.getUniqueId(), executorService);
    }
    if(sender instanceof ConsoleCommandSender) {
      return CompletableFuture.supplyAsync(()->createFullFilled(CommonUtil.getNilUniqueId(), "CONSOLE", false));
    }
    return CompletableFuture.supplyAsync(()->createFullFilled(CommonUtil.getNilUniqueId(), sender.getName(), false));
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final CommandSender sender, final ExecutorService executorService) {

    if(sender instanceof Player player) {
      return createFullFilled(player);
    }
    if(sender instanceof OfflinePlayer offlinePlayer) {
      return createSync(finder, offlinePlayer.getUniqueId(), executorService);
    }
    if(sender instanceof ConsoleCommandSender) {
      return createFullFilled(CommonUtil.getNilUniqueId(), "CONSOLE", false);
    }
    return createFullFilled(CommonUtil.getNilUniqueId(), sender.getName(), false);
  }


  public static CompletableFuture<QUser> createAsync(@NotNull final PlayerFinder finder, @NotNull final CommandSender sender) {

    return createAsync(finder, sender, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  public static QUser createSync(@NotNull final PlayerFinder finder, @NotNull final CommandSender sender) {

    return createSync(finder, sender, QuickExecutor.getPrimaryProfileIoExecutor());
  }

  @Override
  public int hashCode() {

    return Objects.hash(username, uniqueId, realPlayer);
  }

  @Override
  public boolean equals(final Object obj) {

    if(obj instanceof QUser qUser) {
      if(this.isRealPlayer() != qUser.isRealPlayer()) {
        return false;
      }
      if(this.isRealPlayer()) {
        return Objects.equals(this.uniqueId, qUser.getUniqueId());
      } else {
        return Objects.equals(this.username, qUser.getUsername());
      }
    }
    return false;
  }

  @Override
  public String toString() {

    return getDisplay();
  }
}

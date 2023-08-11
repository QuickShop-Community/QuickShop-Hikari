package com.ghostchu.quickshop.obj;

import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.obj.QUser;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class QUserImpl implements QUser {
    private static final long VERSION = 1;
    private static final Cache<Object, QUserImpl> QUSER_CACHE =
            CacheBuilder.newBuilder()
                    .initialCapacity(150)
                    .expireAfterAccess(12, TimeUnit.HOURS)
                    .recordStats()
                    .build();
    private final PlayerFinder finder;
    private String username;
    private UUID uniqueId;
    private boolean realPlayer;

    private QUserImpl(PlayerFinder finder, String string) {
        this.finder = finder;
        parseString(string);
    }

    private QUserImpl(PlayerFinder finder, UUID uuid) {
        this.finder = finder;
        parseString(uuid.toString());
    }

    private QUserImpl(UUID uuid, String username, boolean realPlayer) {
        this.finder = null;
        this.uniqueId = uuid;
        this.username = username;
        this.realPlayer = realPlayer;
    }

    private void parseString(String string) {
        if (CommonUtil.isUUID(string) || CommonUtil.isTrimmedUUID(string)) {
            if (CommonUtil.isTrimmedUUID(string)) {
                string = CommonUtil.fromTrimmedUUID(string).toString();
            }
            this.realPlayer = true;
            this.uniqueId = UUID.fromString(string);
            this.finder.uuid2NameFuture(this.uniqueId).whenComplete((result, throwable) -> {
                if (result != null) {
                    this.username = result;
                }
                if (throwable != null) {
                    Log.debug(Level.WARNING, "Failed to get username from uuid:" + throwable.getMessage());
                }
            });
        }
        if (isBracketedString(string)) {
            String unbracketedString = removeBrackets(string);
            this.realPlayer = false;
            this.uniqueId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + unbracketedString).getBytes(StandardCharsets.UTF_8));
            this.username = unbracketedString;
        } else {
            this.realPlayer = true;
            this.username = string;
            this.uniqueId = this.finder.name2Uuid(username);
            if (this.uniqueId == null) {
                throw new IllegalArgumentException("Cannot find uuid from username:" + username);
            }
        }
        tryToFill();
    }

    private void tryToFill() {
        if (isRealPlayer() && this.username == null) {
            this.username = this.finder.uuid2Name(this.uniqueId);
        }
    }

    private boolean isBracketedString(String input) {
        return input.startsWith("[") && input.endsWith("]");
    }

    private String removeBrackets(String input) {
        if (isBracketedString(input)) {
            return input.substring(1, input.length() - 1);
        }
        return input;
    }

    @Override
    public @Nullable String getUsername() {
        return this.username;
    }

    @Override
    public @Nullable Optional<String> getUsernameOptional() {
        return Optional.ofNullable(this.username);
    }

    @Override
    public @NotNull String getDisplay() {
        if (this.username != null) {
            if (isRealPlayer()) {
                return this.username;
            } else {
                return "[" + this.username + "]";
            }
        }
        return this.uniqueId.toString();
    }

    @Override
    public boolean isFull() {
        if (isRealPlayer()) {
            return this.username != null && this.uniqueId != null;
        }
        return this.username != null;
    }

    @Override
    public @Nullable UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public @Nullable Optional<UUID> getUniqueIdOptional() {
        return Optional.ofNullable(this.uniqueId);
    }

    @Override
    public boolean isRealPlayer() {
        return this.realPlayer && this.uniqueId != null;
    }

    @Override
    public Optional<UUID> getUniqueIdIfRealPlayer() {
        if (isRealPlayer()) {
            return Optional.of(this.uniqueId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameIfRealPlayer() {
        if (isRealPlayer()) {
            return Optional.of(this.username);
        }
        return Optional.empty();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void setUniqueId(UUID uuid) {
        this.uniqueId = uuid;
    }

    @Override
    public void setRealPlayer(boolean isRealPlayer) {
        if (this.uniqueId == null) {
            throw new IllegalStateException("Before set a QUser to realplayer, the uniqueId must be filled");
        }
        this.realPlayer = isRealPlayer;
    }

    @Override
    public String serialize() {
        return VERSION + ";" + this.getUniqueId() + ";" + this.username + ";" + this.realPlayer;
    }

    public static QUserImpl deserialize(PlayerFinder finder, String serialized) {
        String[] split = serialized.split(";");
        if (split.length != 4) {
            // plain text?
            Log.debug("Loading QUser from plain text:" + serialized);
            return new QUserImpl(finder, serialized);
        }
        if (Long.parseLong(split[0]) != VERSION) {
            throw new IllegalArgumentException("Invalid serialized QUser version");
        }
        UUID uuid = null;
        if (!StringUtils.isEmpty(split[1])) {
            uuid = UUID.fromString(split[1]);
        }
        String username = null;
        if (!StringUtils.isEmpty(split[2])) {
            username = split[2];
        }
        boolean realPlayer = Boolean.parseBoolean(split[3]);
        return new QUserImpl(uuid, username, realPlayer);
    }


    public void set(String string) {
        parseString(string);
    }

    public static CompletableFuture<QUser> createAsync(@NotNull PlayerFinder finder, @NotNull String string) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return QUSER_CACHE.get(string, () -> new QUserImpl(finder, string));
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }, QuickExecutor.getProfileIOExecutor());
    }

    public static CompletableFuture<QUser> createAsync(@NotNull PlayerFinder finder, @NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return QUSER_CACHE.get(uuid.toString(), () -> new QUserImpl(finder, uuid.toString()));
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }, QuickExecutor.getProfileIOExecutor());
    }

    public static QUser createFullFilled(UUID uuid, String username, boolean realPlayer) {
        return new QUserImpl(uuid, username, realPlayer);
    }

    public static QUser createFullFilled(Player player) {
        return new QUserImpl(player.getUniqueId(), player.getName(), true);
    }

    public static QUser createSync(@NotNull PlayerFinder finder, @NotNull String string) {
        try {
            return QUSER_CACHE.get(string, () -> new QUserImpl(finder, string));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static QUser createSync(@NotNull PlayerFinder finder, @NotNull UUID uuid) {
        try {
            return QUSER_CACHE.get(uuid.toString(), () -> new QUserImpl(finder, uuid.toString()));
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    public static CompletableFuture<QUser> createAsync(@NotNull PlayerFinder finder, @NotNull CommandSender sender) {
        if (sender instanceof Player player) {
            return CompletableFuture.supplyAsync(() -> createFullFilled(player));
        }
        if (sender instanceof OfflinePlayer offlinePlayer) {
            return createAsync(finder, offlinePlayer.getUniqueId());
        }
        if (sender instanceof ConsoleCommandSender) {
            return CompletableFuture.supplyAsync(() -> createFullFilled(CommonUtil.getNilUniqueId(), "CONSOLE", false));
        }
        return CompletableFuture.supplyAsync(() -> createFullFilled(CommonUtil.getNilUniqueId(), sender.getName(), false));
    }

    public static QUser createSync(@NotNull PlayerFinder finder, @NotNull CommandSender sender) {
        if (sender instanceof Player player) {
            return createFullFilled(player);
        }
        if (sender instanceof OfflinePlayer offlinePlayer) {
            return createSync(finder, offlinePlayer.getUniqueId());
        }
        if (sender instanceof ConsoleCommandSender) {
            return createFullFilled(CommonUtil.getNilUniqueId(), "CONSOLE", false);
        }
        return createFullFilled(CommonUtil.getNilUniqueId(), sender.getName(), false);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QUser qUser) {
            if (this.isRealPlayer() != qUser.isRealPlayer()) return false;
            if (this.isRealPlayer()) {
                return this.uniqueId.equals(qUser.getUniqueId()) && this.username.equalsIgnoreCase(qUser.getUsername());
            } else {
                return this.username.equalsIgnoreCase(qUser.getUsername());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getDisplay();
    }

    public static Cache<Object, QUserImpl> getQuserCache() {
        return QUSER_CACHE;
    }
}

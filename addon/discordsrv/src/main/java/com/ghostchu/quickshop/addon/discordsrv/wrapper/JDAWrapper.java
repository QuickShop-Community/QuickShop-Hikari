package com.ghostchu.quickshop.addon.discordsrv.wrapper;

import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface JDAWrapper {
    boolean isBind(@NotNull UUID player);

    void sendMessage(@NotNull UUID player, @NotNull String message);

    void sendMessage(@NotNull UUID player, @NotNull MessageEmbed message);

    void sendChannelMessage(@NotNull String channelId, @NotNull String message);

    void sendChannelMessage(@NotNull String channelId, @NotNull MessageEmbed message);
}

package com.ghostchu.quickshop.obj;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.PlayerFinder;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.ghostchu.quickshop.common.util.QuickExecutor;
import com.ghostchu.quickshop.util.logger.Log;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
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
import java.util.logging.Level;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public final class QUserSimpleRecord {
    private String username;
    private UUID uniqueId;
    private boolean realPlayer;

    public QUserSimpleRecord(QUser qUser) {
        if (qUser == null) {
            this.uniqueId = CommonUtil.getNilUniqueId();
            this.username = "<N/A>";
            this.realPlayer = false;
        } else {
            this.uniqueId = qUser.getUniqueId();
            this.username = qUser.getUsername();
            this.realPlayer = qUser.isRealPlayer();
        }
    }


    public static QUserSimpleRecord wrap(QUser qUser) {
        return new QUserSimpleRecord(qUser);
    }
}

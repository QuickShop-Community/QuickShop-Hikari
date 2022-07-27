package com.ghostchu.quickshop.permission;

import lombok.Data;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class PermissionInformationContainer {
    @NotNull
    private CommandSender sender;

    @NotNull
    private String permission;

    @Nullable
    private String groupName;

    @Nullable
    private String otherInfos;

    public PermissionInformationContainer(@NotNull CommandSender sender, @NotNull String permission, @Nullable String groupName, @Nullable String otherInfos) {
        this.sender = sender;
        this.permission = permission;
        this.groupName = groupName;
        this.otherInfos = otherInfos;
    }

    /**
     * Get sender is console
     *
     * @return yes or no
     */
    public boolean isConsole() {
        return sender instanceof Server;
    }

}

package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * WarningSender to prevent send too many warnings to CommandSender in short time.
 *
 * @author Ghost_chu
 */
@EqualsAndHashCode
@ToString
public class WarningSender {
    private final long cooldown;
    @ToString.Exclude
    private final QuickShop plugin;
    private long lastSend = 0;

    /**
     * Create a warning sender
     *
     * @param plugin   Main class
     * @param cooldown Time unit: ms
     */
    public WarningSender(@NotNull QuickShop plugin, long cooldown) {
        this.plugin = plugin;
        this.cooldown = cooldown;
    }

    /**
     * Send warning a warning
     *
     * @param text The text you want send/
     * @return Success sent, if it is in a cool-down, it will return false
     */
    public boolean sendWarn(String text) {
        if (System.currentTimeMillis() - lastSend > cooldown) {
            plugin.getLogger().warning(text);
            this.lastSend = System.currentTimeMillis();
            return true;
        }
        return false;
    }

}

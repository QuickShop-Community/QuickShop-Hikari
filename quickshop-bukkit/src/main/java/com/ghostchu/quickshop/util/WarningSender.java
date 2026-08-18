package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.QuickShop;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * WarningSender to prevent send too many warnings to CommandSender in short time.
 *
 * @author Ghost_chu
 */
public class WarningSender {

  private final long cooldown;
  private final QuickShop plugin;
  private long lastSend = 0;

  /**
   * Create a warning sender
   *
   * @param plugin   Main class
   * @param cooldown Time unit: ms
   */
  public WarningSender(@NotNull final QuickShop plugin, final long cooldown) {

    this.plugin = plugin;
    this.cooldown = cooldown;
  }

  /**
   * Send warning a warning
   *
   * @param text The text you want send/
   *
   * @return Success sent, if it is in a cool-down, it will return false
   */
  public boolean sendWarn(final String text) {

    if(System.currentTimeMillis() - lastSend > cooldown) {
      plugin.logger().warn(text);
      this.lastSend = System.currentTimeMillis();
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof WarningSender)) return false;
    final WarningSender other = (WarningSender)o;
    return this.cooldown == other.cooldown
           && this.lastSend == other.lastSend
           && Objects.equals(this.plugin, other.plugin);
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.cooldown, this.lastSend, this.plugin);
  }

  @Override
  public String toString() {

    return "WarningSender(cooldown=" + this.cooldown + ", lastSend=" + this.lastSend + ")";
  }
}

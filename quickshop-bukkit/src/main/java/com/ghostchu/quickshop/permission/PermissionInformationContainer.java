package com.ghostchu.quickshop.permission;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PermissionInformationContainer {

  @NotNull
  private CommandSender sender;

  @NotNull
  private String permission;

  @Nullable
  private String groupName;

  @Nullable
  private String otherInfos;

  public PermissionInformationContainer(@NotNull final CommandSender sender, @NotNull final String permission, @Nullable final String groupName, @Nullable final String otherInfos) {

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

  @NotNull
  public CommandSender getSender() {

    return this.sender;
  }

  @NotNull
  public String getPermission() {

    return this.permission;
  }

  @Nullable
  public String getGroupName() {

    return this.groupName;
  }

  @Nullable
  public String getOtherInfos() {

    return this.otherInfos;
  }

  public void setSender(@NotNull final CommandSender sender) {

    if(sender == null) {
      throw new NullPointerException("sender is marked non-null but is null");
    }
    this.sender = sender;
  }

  public void setPermission(@NotNull final String permission) {

    if(permission == null) {
      throw new NullPointerException("permission is marked non-null but is null");
    }
    this.permission = permission;
  }

  public void setGroupName(@Nullable final String groupName) {

    this.groupName = groupName;
  }

  public void setOtherInfos(@Nullable final String otherInfos) {

    this.otherInfos = otherInfos;
  }

  @Override
  public boolean equals(final Object o) {

    if(o == this) return true;
    if(!(o instanceof PermissionInformationContainer)) return false;
    final PermissionInformationContainer other = (PermissionInformationContainer)o;
    return Objects.equals(this.getSender(), other.getSender())
           && Objects.equals(this.getPermission(), other.getPermission())
           && Objects.equals(this.getGroupName(), other.getGroupName())
           && Objects.equals(this.getOtherInfos(), other.getOtherInfos());
  }

  @Override
  public int hashCode() {

    return Objects.hash(this.getSender(), this.getPermission(), this.getGroupName(), this.getOtherInfos());
  }

  @Override
  public String toString() {

    return "PermissionInformationContainer(sender=" + this.getSender() + ", permission=" + this.getPermission() + ", groupName=" + this.getGroupName() + ", otherInfos=" + this.getOtherInfos() + ")";
  }
}

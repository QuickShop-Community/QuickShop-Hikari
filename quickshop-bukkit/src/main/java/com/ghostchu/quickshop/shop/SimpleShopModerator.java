package com.ghostchu.quickshop.shop;

import com.ghostchu.quickshop.api.shop.ShopModerator;
import com.ghostchu.quickshop.common.util.JsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Contains shop's moderators infomations, owner, staffs etc. You must save the ContainerShop after
 * modify this
 */
@EqualsAndHashCode
@NoArgsConstructor
@Deprecated
public class SimpleShopModerator implements ShopModerator {

  private UUID owner;
  private List<UUID> staffs;

  /**
   * Shop moderators, inlucding owner, and empty staffs.
   *
   * @param owner The owner
   */
  public SimpleShopModerator(@NotNull final UUID owner) {

    this.owner = owner;
    this.staffs = new ArrayList<>();
  }

  /**
   * Shop moderators, inlucding owner, staffs.
   *
   * @param owner  The owner
   * @param staffs The staffs
   */
  public SimpleShopModerator(@NotNull final UUID owner, @NotNull final List<UUID> staffs) {

    this.owner = owner;
    this.staffs = staffs;
  }

  @NotNull
  public static ShopModerator deserialize(@NotNull final String serilized) throws JsonSyntaxException {
    // Use Gson deserialize data
    final Gson gson = JsonUtil.regular();
    return gson.fromJson(serilized, SimpleShopModerator.class);
  }

  /**
   * Add moderators staff to staff list
   *
   * @param player New staff
   *
   * @return Success
   */
  @Override
  public boolean addStaff(@NotNull final UUID player) {

    if(staffs.contains(player)) {
      return false;
    }
    staffs.add(player);
    return true;
  }

  /**
   * Remove all staffs
   */
  @Override
  public void clearStaffs() {

    staffs.clear();
  }

  /**
   * Remove moderators staff from staff list
   *
   * @param player Staff
   *
   * @return Success
   */
  @Override
  public boolean delStaff(@NotNull final UUID player) {

    return staffs.remove(player);
  }

  /**
   * Get moderators owner (Shop Owner).
   *
   * @return Owner's UUID
   */
  @Override
  public @NotNull UUID getOwner() {

    return owner;
  }

  /**
   * Set moderators owner (Shop Owner)
   *
   * @param player Owner's UUID
   */
  @Override
  public void setOwner(@NotNull final UUID player) {

    this.owner = player;
  }

  /**
   * Get staffs list
   *
   * @return Staffs
   */
  @Override
  public @NotNull List<UUID> getStaffs() {

    return staffs;
  }

  /**
   * Set moderators staffs
   *
   * @param players staffs list
   */
  @Override
  public void setStaffs(@NotNull final List<UUID> players) {

    this.staffs = players;
  }

  /**
   * Get a player is or not moderators
   *
   * @param player Player
   *
   * @return yes or no, return true when it is staff or owner
   */
  @Override
  public boolean isModerator(@NotNull final UUID player) {

    return isOwner(player) || isStaff(player);
  }

  /**
   * Get a player is or not moderators owner
   *
   * @param player Player
   *
   * @return yes or no
   */
  @Override
  public boolean isOwner(@NotNull final UUID player) {

    return player.equals(owner);
  }

  /**
   * Get a player is or not moderators a staff
   *
   * @param player Player
   *
   * @return yes or no
   */
  @Override
  public boolean isStaff(@NotNull final UUID player) {

    return staffs.contains(player);
  }

  @Override
  public @NotNull String toString() {

    return serialize(this);
  }

  @NotNull
  public static String serialize(@NotNull final ShopModerator shopModerator) {

    final Gson gson = JsonUtil.getGson();
    final SimpleShopModerator gsonWorkaround = (SimpleShopModerator)shopModerator;
    return gson.toJson(gsonWorkaround); // Use Gson serialize this class
  }

}

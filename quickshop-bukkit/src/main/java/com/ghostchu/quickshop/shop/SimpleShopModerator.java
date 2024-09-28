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
  public SimpleShopModerator(@NotNull UUID owner) {

    this.owner = owner;
    this.staffs = new ArrayList<>();
  }

  /**
   * Shop moderators, inlucding owner, staffs.
   *
   * @param owner  The owner
   * @param staffs The staffs
   */
  public SimpleShopModerator(@NotNull UUID owner, @NotNull List<UUID> staffs) {

    this.owner = owner;
    this.staffs = staffs;
  }

  @NotNull
  public static ShopModerator deserialize(@NotNull String serilized) throws JsonSyntaxException {
    // Use Gson deserialize data
    Gson gson = JsonUtil.regular();
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
  public boolean addStaff(@NotNull UUID player) {

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
  public boolean delStaff(@NotNull UUID player) {

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
  public void setOwner(@NotNull UUID player) {

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
  public void setStaffs(@NotNull List<UUID> players) {

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
  public boolean isModerator(@NotNull UUID player) {

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
  public boolean isOwner(@NotNull UUID player) {

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
  public boolean isStaff(@NotNull UUID player) {

    return staffs.contains(player);
  }

  @Override
  public @NotNull String toString() {

    return serialize(this);
  }

  @NotNull
  public static String serialize(@NotNull ShopModerator shopModerator) {

    Gson gson = JsonUtil.getGson();
    SimpleShopModerator gsonWorkaround = (SimpleShopModerator)shopModerator;
    return gson.toJson(gsonWorkaround); // Use Gson serialize this class
  }

}

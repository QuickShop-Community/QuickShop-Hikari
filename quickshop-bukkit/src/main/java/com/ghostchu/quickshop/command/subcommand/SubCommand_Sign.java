package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SubCommand_Sign implements CommandHandler<Player> {

  private final QuickShop plugin;

  public SubCommand_Sign(QuickShop plugin) {

    this.plugin = plugin;
  }

  @Override
  public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {

    Shop shop = getLookingShop(sender);
    if(shop == null) {
      plugin.text().of(sender, "not-looking-at-shop").send();
      return;
    }
    if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SIGN_TYPE) && !plugin.perm().hasPermission(sender, "quickshop.other.sign")) {
      plugin.text().of(sender, "no-permission");
      return;
    }

    if(parser.getArgs().isEmpty()) {
      plugin.text().of(sender, "no-sign-type-given", CommonUtil.list2String(getAvailableSignMaterials().stream().map(Enum::name).toList())).send();
      return;
    }
    String signType = parser.getArgs().get(0);
    Material material = Material.matchMaterial(signType.trim());
    if(material == null || !Tag.WALL_SIGNS.isTagged(material)) {
      plugin.text().of(sender, "sign-type-invalid", signType).send();
      return;
    }
    for(Sign sign : shop.getSigns()) {
      plugin.getShopManager().makeShopSign(shop.getLocation().getBlock(), sign.getBlock(), material);
      shop.claimShopSign(sign);
    }
    shop.setSignText(plugin.text().findRelativeLanguages(sender));
  }

  private Set<Material> getAvailableSignMaterials() {

    return Tag.WALL_SIGNS.getValues();
  }

  @NotNull
  @Override
  public List<String> onTabComplete(
          @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {

    return parser.getArgs().size() == 1? Tag.WALL_SIGNS.getValues().stream().map(Enum::name).toList() : Collections.emptyList();
  }

}

package com.ghostchu.quickshop.command.subcommand;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.command.CommandHandler;
import com.ghostchu.quickshop.api.command.CommandParser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermission;
import com.ghostchu.quickshop.common.util.CommonUtil;
import com.ghostchu.quickshop.util.logger.Log;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubCommand_Sign implements CommandHandler<Player> {

    private final QuickShop plugin;

    public SubCommand_Sign(QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        Shop shop = getLookingShop(sender);
        if (shop == null) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        if(!shop.playerAuthorize(sender.getUniqueId(), BuiltInShopPermission.SET_SIGN_TYPE) && ! plugin.perm().hasPermission(sender, "quickshop.other.sign")){
            plugin.text().of(sender,"no-permission");
            return;
        }

        if (parser.getArgs().isEmpty()) {
            plugin.text().of(sender, "no-sign-type-given", CommonUtil.list2String(getAvailableSignMaterials().stream().map(Enum::name).toList())).send();
            return;
        }
        String signType = parser.getArgs().get(0);
        Material material = Material.matchMaterial(signType.trim());
        if(material == null){
            plugin.text().of(sender,"sign-type-invalid", signType).send();
            return;
        }
        material = mapToItemSign(material);
        if(material == null){
            plugin.text().of("internal-error").send();
            plugin.logger().warn("Failed to map {}: Target material are not exists.", signType);
            return;
        }
        for (Sign sign : shop.getSigns()) {
            plugin.getShopManager().makeShopSign(shop.getLocation().getBlock(), sign.getBlock(), material);
            shop.claimShopSign(sign);
        }
        shop.setSignText(plugin.text().findRelativeLanguages(sender));
    }

    @Nullable
    private Material mapToItemSign(Material wallSign){
        String wallSignName = wallSign.name();
        String signName = wallSignName.replace("_WALL_SIGN", "_SIGN");
        Material material = Material.matchMaterial(signName);
        if(material == null){
            Log.debug("Material "+wallSignName+" mapping to "+signName+" failed. The target material are not exists.");
            return null;
        }
        return material;
    }

    private List<Material> cachedSignMaterials = null;

    private List<Material> getAvailableSignMaterials() {
        if (cachedSignMaterials != null) {
            return cachedSignMaterials;
        }
        List<Material> materials = new ArrayList<>();
        for (Material value : Material.values()) {
            if (Tag.WALL_SIGNS.isTagged(value)) {
                materials.add(value);
            }
        }
        cachedSignMaterials = materials;
        return materials;
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull CommandParser parser) {
        return parser.getArgs().size() == 1 ? Collections.singletonList(plugin.text().of(sender, "tabcomplete.price").plain()) : Collections.emptyList();
    }

}

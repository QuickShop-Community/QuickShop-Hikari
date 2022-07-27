package com.ghostchu.quickshop.shop.datatype;

import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DisplayItemPersistentDataType
        implements PersistentDataType<String, ShopProtectionFlag> {
    static final DisplayItemPersistentDataType INSTANCE = new DisplayItemPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ShopProtectionFlag> getComplexType() {
        return ShopProtectionFlag.class;
    }

    @NotNull
    @Override
    public String toPrimitive(
            @NotNull ShopProtectionFlag complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return JsonUtil.getGson().toJson(complex);
        } catch (Exception th) {
            new RuntimeException("Cannot to toPrimitive the shop protection flag.").printStackTrace();
            return "";
        }
    }

    @NotNull
    @Override
    public ShopProtectionFlag fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return JsonUtil.getGson().fromJson(primitive, ShopProtectionFlag.class);
        } catch (Exception th) {
            new RuntimeException("Cannot to fromPrimitive the shop protection flag.").printStackTrace();
            return new ShopProtectionFlag("", Util.serialize(new ItemStack(Material.STONE)));
        }
    }

}

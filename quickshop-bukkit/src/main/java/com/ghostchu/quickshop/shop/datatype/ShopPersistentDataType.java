package com.ghostchu.quickshop.shop.datatype;

import com.ghostchu.quickshop.util.MsgUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ShopPersistentDataType implements PersistentDataType<String, ShopPersistentData> {
    static final ShopPersistentDataType INSTANCE = new ShopPersistentDataType();

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<ShopPersistentData> getComplexType() {
        return ShopPersistentData.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull ShopPersistentData complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.toJson(complex);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return "";
        }
    }

    @NotNull
    @Override
    public ShopPersistentData fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.fromJson(primitive, ShopPersistentData.class);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return new ShopPersistentData(0, 0, 0, "null", false);
        }
    }
}

package com.ghostchu.quickshop.shop.datatype;

import com.ghostchu.quickshop.util.MsgUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HopperPersistentDataType implements PersistentDataType<String, HopperPersistentData> {
    public static final HopperPersistentDataType INSTANCE = new HopperPersistentDataType();

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<HopperPersistentData> getComplexType() {
        return HopperPersistentData.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull HopperPersistentData complex, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.toJson(complex);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return "";
        }
    }

    @NotNull
    @Override
    public HopperPersistentData fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return GSON.fromJson(primitive, HopperPersistentData.class);
        } catch (Exception th) {
            MsgUtil.debugStackTrace(th.getStackTrace());
            return new HopperPersistentData(new UUID(0, 0));
        }
    }
}

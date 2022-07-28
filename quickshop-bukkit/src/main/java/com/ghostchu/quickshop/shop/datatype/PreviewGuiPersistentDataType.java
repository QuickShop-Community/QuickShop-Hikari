package com.ghostchu.quickshop.shop.datatype;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PreviewGuiPersistentDataType
        implements PersistentDataType<String, UUID> {
    public static final PreviewGuiPersistentDataType INSTANCE = new PreviewGuiPersistentDataType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @NotNull
    @Override
    public String toPrimitive(
            @NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
        return complex.toString();
    }

    @NotNull
    @Override
    public UUID fromPrimitive(
            @NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
        try {
            return UUID.fromString(primitive);
        } catch (Exception exception) {
            return new UUID(0L, 0L);
        }
    }

}

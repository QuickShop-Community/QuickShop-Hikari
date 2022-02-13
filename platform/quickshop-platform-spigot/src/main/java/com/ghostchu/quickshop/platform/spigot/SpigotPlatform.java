package com.ghostchu.quickshop.platform.spigot;

import de.tr7zw.nbtapi.NBTTileEntity;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.ghostchu.quickshop.platform.Platform;

public class SpigotPlatform implements Platform {
    private NBTAPI nbtapi;
    private final ReflServerStateProvider provider;

    public SpigotPlatform() {
        this.provider = new ReflServerStateProvider();
        if (Bukkit.getPluginManager().isPluginEnabled("NBTAPI")) {
            nbtapi = NBTAPI.getInstance();
        }
    }

    @Override
    public void setLine(@NotNull Sign sign, int line, @NotNull Component component) {
        if (this.nbtapi != null) {
            NBTTileEntity tileSign = new NBTTileEntity(sign);
            try {
                tileSign.setString("Text" + (line + 1), GsonComponentSerializer.gson().serialize(component));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sign.setLine(line, LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public @NotNull Component getLine(@NotNull Sign sign, int line) {
        return LegacyComponentSerializer.legacySection().deserialize(sign.getLine(line));
    }

    @Override
    public @NotNull TranslatableComponent getItemTranslationKey(@NotNull Material material) {
        return Component.translatable(ReflectFactory.getMaterialMinecraftNamespacedKey(material));
    }

    @Override
    public @NotNull HoverEvent<HoverEvent.ShowItem> getItemStackHoverEvent(@NotNull ItemStack stack) {
        NamespacedKey namespacedKey = stack.getType().getKey();
        Key key = Key.key(namespacedKey.toString());
        return HoverEvent.showItem(key,stack.getAmount(), BinaryTagHolder.of(ReflectFactory.getMaterialMinecraftNamespacedKey(stack.getType())));
    }

    @Override
    public void registerCommand(@NotNull String prefix, @NotNull PluginCommand command) {
        try{
            ReflectFactory.getCommandMap().register(prefix, command);
            ReflectFactory.syncCommands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isServerStopping() {
        return this.provider.isStopping();
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return ReflectFactory.getServerVersion();
    }
}

package com.ghostchu.quickshop.addon.discordsrv.message;

import com.ghostchu.quickshop.addon.discordsrv.Main;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MessageManager {
    private final Main plugin;
    private final Map<String, Map.Entry<Object, Method>> embedMessageRegistry = new HashMap<>();
    private final Map<String, Map.Entry<Object, Method>> regularMessageRegistry = new HashMap<>();

    public MessageManager(Main plugin) {
        this.plugin = plugin;
    }

    public void register(@NotNull Object obj) {
        plugin.getLogger().info("Performing class scanning...");
        scanClass(obj);
    }

    @NotNull
    private MessageEmbed failSafeEmbedMessage(@NotNull String key) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":question: Unknown embed message");
        builder.setDescription("Sorry! We can't find the process message `" + key + "`, please report this to the server administrators!");
        builder.setColor(Color.GRAY);
        builder.setFooter("Handle - failedSafeEmbedMessage - failsafe - default");
        builder.setThumbnail(null);
        builder.setImage(null);
        builder.setAuthor(null);
        return builder.build();
    }

    @NotNull
    private String failSafeMessage(@NotNull String key) {
        return "**Unknown embed message**: " + "Sorry! We can't process the embed message `" + key + "`, please report this to the server administrators!";
    }


    @NotNull
    public MessageEmbed getEmbedMessage(@NotNull String key, @Nullable UUID receiver, @NotNull Map<String, String> placeholders) {
        Map.Entry<Object, Method> method = embedMessageRegistry.get(key);
        if (method == null) {
            plugin.getLogger().warning("Cannot find embed message: " + key);
            return failSafeEmbedMessage(key);
        }
        try {
            Object returns = method.getValue().invoke(method.getKey(), receiver, placeholders);
            if (!(returns instanceof MessageEmbed embed)) {
                plugin.getLogger().log(Level.WARNING, "Cannot handle embed message: " + key + " Mismatched type!");
                return failSafeEmbedMessage(key);
            }
            return embed;
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot handle embed message: " + key, e);
            return failSafeEmbedMessage(key);
        }
    }

    @NotNull
    public String getRegularMessage(@NotNull String key, @Nullable UUID receiver, @NotNull Map<String, String> placeholders) {
        Map.Entry<Object, Method> method = regularMessageRegistry.get(key);
        if (method == null) {
            plugin.getLogger().warning("Cannot find message: " + key);
            return failSafeMessage(key);
        }
        try {
            Object returns = method.getValue().invoke(method.getKey(), receiver, placeholders);
            if (!(returns instanceof String msg)) {
                plugin.getLogger().log(Level.WARNING, "Cannot handle message: " + key + " Mismatched type!");
                return failSafeMessage(key);
            }
            return msg;
        } catch (IllegalAccessException | InvocationTargetException e) {
            plugin.getLogger().log(Level.WARNING, "Cannot handle message: " + key, e);
            return failSafeMessage(key);
        }
    }

    public boolean hasEmbedMessage(@NotNull String key) {
        return embedMessageRegistry.containsKey(key);
    }

    public boolean hasRegularMessage(@NotNull String key) {
        return regularMessageRegistry.containsKey(key);
    }

    public void unregisterEmbedMessage(@NotNull String key) {
        embedMessageRegistry.remove(key);
    }

    public void unregisterRegularMessage(@NotNull String key) {
        regularMessageRegistry.remove(key);
    }

    private void scanClass(@Nullable Object obj) {
        if (obj == null) return;
        for (Method declaredMethod : obj.getClass().getDeclaredMethods()) {
            AutoRegisterMessage annotation = declaredMethod.getAnnotation(AutoRegisterMessage.class);
            if (annotation == null) {
                continue;
            }
            if (StringUtils.isEmpty(annotation.key())) {
                continue;
            }
            try {
                if (declaredMethod.getReturnType() == String.class) {
                    this.regularMessageRegistry.put(annotation.key(), new AbstractMap.SimpleEntry<>(obj, declaredMethod));
                } else if (declaredMethod.getReturnType() == MessageEmbed.class) {
                    this.embedMessageRegistry.put(annotation.key(), new AbstractMap.SimpleEntry<>(obj, declaredMethod));
                } else {
                    plugin.getLogger().warning("Skipping message " + obj.getClass().getName() + "." + declaredMethod.getName() + " for registering: Unsupported return type: " + declaredMethod.getReturnType().getName());
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to register message: " + annotation.key(), e);
            }
        }
    }
}

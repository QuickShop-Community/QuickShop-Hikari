/*
 * This file is a part of project QuickShop, the name is QSEventManager.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.eventmanager;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.AuthorNagException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.eventmanager.QuickEventManager;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class QSEventManager implements QuickEventManager, Listener, Reloadable {
    private final QuickShop plugin;
    private final List<ListenerContainer> ignoredListener = new ArrayList<>();

    public QSEventManager(QuickShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.rescan();
    }

    @Override
    public ReloadResult reloadModule() {
        rescan();
        return new ReloadResult(ReloadStatus.SUCCESS,null,null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void serverReloaded(ServerLoadEvent event) {
        this.rescan();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pluginDisable(PluginDisableEvent event) {
        this.rescan();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pluginEnable(PluginEnableEvent event) {
        this.rescan();
    }

    private synchronized void rescan() {
        this.ignoredListener.clear();
        plugin
                .getConfig()
                .getStringList("shop.protection-checking-listener-blacklist")
                .forEach(
                        input -> {
                            if (StringUtils.isEmpty(input)) {
                                return;
                            }
                            try {
                                Class<?> clazz = Class.forName(input);
                                this.ignoredListener.add(new ListenerContainer(clazz, input));
                                Util.debugLog("Successfully added blacklist: [BINDING] " + clazz.getName());
                            } catch (Exception ignored) {
                                this.ignoredListener.add(new ListenerContainer(null, input));
                                Util.debugLog("Successfully added blacklist: [DYNAMIC] " + input);
                            }
                        });
    }

    @Override
    public void callEvent(Event event) {
        if (event.isAsynchronous()) {
            if (Thread.holdsLock(plugin.getServer().getPluginManager())) {
                throw new IllegalStateException(
                        event.getEventName()
                                + " cannot be triggered asynchronously from inside synchronized code.");
            }
            if (Bukkit.getServer().isPrimaryThread()) {
                throw new IllegalStateException(
                        event.getEventName()
                                + " cannot be triggered asynchronously from primary server thread.");
            }
        } else {
            if (!Bukkit.getServer().isPrimaryThread()) {
                throw new IllegalStateException(
                        event.getEventName() + " cannot be triggered asynchronously from another thread.");
            }
        }

        fireEvent(event);
    }

    private void fireEvent(Event event) {
        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }
            Class<?> regClass = registration.getListener().getClass();


            boolean skip = false;
            for (ListenerContainer container : this.ignoredListener) {
                if (container.matches(regClass, registration.getPlugin())) {
                    skip = true;
                    break;
                }
            }

            if (skip) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (AuthorNagException ex) {
                Plugin plugin = registration.getPlugin();

                if (plugin.isNaggable()) {
                    plugin.setNaggable(false);

                    plugin.getServer()
                            .getLogger()
                            .log(
                                    Level.SEVERE,
                                    String.format(
                                            "Nag author(s): '%s' of '%s' about the following: %s",
                                            plugin.getDescription().getAuthors(),
                                            plugin.getDescription().getFullName(),
                                            ex.getMessage()));
                }
            } catch (Throwable ex) {
                plugin.getServer()
                        .getLogger()
                        .log(
                                Level.SEVERE,
                                "Could not pass event "
                                        + event.getEventName()
                                        + " to "
                                        + registration.getPlugin().getDescription().getFullName(),
                                ex);
            }
        }
    }
}

@AllArgsConstructor
class ListenerContainer {
    @Nullable
    private final Class<?> clazz;
    @NotNull
    private final String clazzName;

    public boolean matches(@NotNull Class<?> matching, @NotNull Plugin plugin) {
        if (clazz != null) {
            return matching.equals(clazz);
        }
        if (clazzName.startsWith("@")) {
            return clazzName.equalsIgnoreCase("@" + plugin.getName());
        }
        String name = matching.getName();
        if (name.equalsIgnoreCase(clazzName)) {
            return true;
        }
        if (name.startsWith(clazzName)) {
            return true;
        }
        return name.matches(clazzName);
    }
}

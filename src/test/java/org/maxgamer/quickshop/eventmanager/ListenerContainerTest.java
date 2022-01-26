/*
 * This file is a part of project QuickShop, the name is ListenerContainerTest.java
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


import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class ListenerContainerTest {

    @Test
    public void testMatches() {
        Plugin testPlugin = new Plugin() {
            @NotNull
            @Override
            public File getDataFolder() {
                return null;
            }

            @NotNull
            @Override
            public PluginDescriptionFile getDescription() {
                return null;
            }

            @NotNull
            @Override
            public FileConfiguration getConfig() {
                return null;
            }

            @NotNull
            public FileConfiguration getConfiguration() {
                return null;
            }

            @Nullable
            @Override
            public InputStream getResource(@NotNull String s) {
                return null;
            }

            @Override
            public void saveConfig() {

            }

            @Override
            public void saveDefaultConfig() {

            }

            @Override
            public void saveResource(@NotNull String s, boolean b) {

            }

            @Override
            public void reloadConfig() {

            }

            @NotNull
            @Override
            public PluginLoader getPluginLoader() {
                return null;
            }

            @NotNull
            @Override
            public Server getServer() {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void onDisable() {

            }

            @Override
            public void onLoad() {

            }

            @Override
            public void onEnable() {

            }

            @Override
            public boolean isNaggable() {
                return false;
            }

            @Override
            public void setNaggable(boolean b) {

            }

            @Nullable
            @Override
            public ChunkGenerator getDefaultWorldGenerator(@NotNull String s, @Nullable String s1) {
                return null;
            }

            @Nullable
            @Override
            public BiomeProvider getDefaultBiomeProvider(@NotNull String s, @Nullable String s1) {
                return null;
            }

            @NotNull
            @Override
            public Logger getLogger() {
                return null;
            }

            @NotNull
            @Override
            public String getName() {
                return "QuickTest";
            }

            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                return false;
            }

            @Nullable
            @Override
            public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                return null;
            }
        };
        ListenerContainer listenerContainerA = new ListenerContainer(null, "@QuickTest");
        ListenerContainer listenerContainerB = new ListenerContainer(null, "@QuickTestBad");
        Assertions.assertTrue(listenerContainerA.matches(this.getClass(), testPlugin));
        Assertions.assertFalse(listenerContainerB.matches(this.getClass(), testPlugin));
    }

}
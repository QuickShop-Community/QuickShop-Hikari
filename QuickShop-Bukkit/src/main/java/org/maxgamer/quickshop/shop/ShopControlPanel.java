package org.maxgamer.quickshop.shop;///*
// * This file is a part of project QuickShop, the name is ShopControlPanel.java
// *  Copyright (C) PotatoCraft Studio and contributors
// *
// *  This program is free software: you can redistribute it and/or modify it
// *  under the terms of the GNU General Public License as published by the
// *  Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful, but WITHOUT
// *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *  for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program. If not, see <http://www.gnu.org/licenses/>.
// *
// */
//
//package org.maxgamer.quickshop.shop;
//
//import lombok.EqualsAndHashCode;
//import org.bukkit.command.CommandSender;
//import org.bukkit.plugin.Plugin;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//import org.maxgamer.quickshop.QuickShop;
//
//import java.io.Serializable;
//import java.util.Map;
//import java.util.TreeSet;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.function.Function;
//
////import org.maxgamer.quickshop.api.chat.QuickComponent;
//
//public class ShopControlPanel {
//    private final Map<Plugin, TreeSet<Entry>> panelRegistry = new ConcurrentHashMap<>();
//    private final QuickShop plugin;
//
//    public ShopControlPanel(QuickShop plugin) {
//        this.plugin = plugin;
//    }
//
//
//    /**
//     * Register the ControlPanel Entry to registry
//     *
//     * @param namespace The plugin instance
//     * @param priority  The entry priority
//     * @param executor  The executor, should returns an QuickComponent or null if no out-put needs
//     */
//    public void register(@NotNull Plugin namespace, int priority, @NotNull Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> executor) {
//        TreeSet<Entry> entries = this.panelRegistry.get(namespace);
//        if (entries == null) {
//            entries = new TreeSet<>(new Comparator());
//        }
//        Entry entry = new Entry(priority, executor);
//        if (entries.contains(entry)) {
//            return;
//        }
//        entries.add(entry);
//        this.panelRegistry.put(namespace, entries);
//    }
//
//    /**
//     * Unregister a registered executor from registry
//     *
//     * @param namespace The plugin instance
//     * @param priority  The entry priority
//     * @param executor  The executor, should returns an QuickComponent or null if no out-put needs
//     */
//    public void unregister(@NotNull Plugin namespace, int priority, @NotNull Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> executor) {
//        TreeSet<Entry> entries = this.panelRegistry.get(namespace);
//        if (entries == null) {
//            return;
//        }
//        Entry entry = new Entry(priority, executor);
//        entries.remove(entry);
//    }
//
//    /**
//     * Unregister a registered executor from registry
//     *
//     * @param namespace The plugin instance
//     * @param executor  The executor, should returns an QuickComponent or null if no out-put needs
//     */
//    public void unregister(@NotNull Plugin namespace, @NotNull Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> executor) {
//        TreeSet<Entry> entries = this.panelRegistry.get(namespace);
//        if (entries == null) {
//            return;
//        }
//        entries.removeIf(entry -> entry.getExecutor().equals(executor));
//    }
//
//    /**
//     * Unregister plugin's all registered executor from registry
//     *
//     * @param namespace The plugin instance
//     */
//    public void unregisterAll(@NotNull Plugin namespace) {
//        this.panelRegistry.remove(namespace);
//    }
//
//    @EqualsAndHashCode
//    public static class Entry {
//        private final int priority;
//        private final Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> executor;
//
//        public Entry(int priority, @NotNull Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> executor) {
//            this.priority = priority;
//            this.executor = executor;
//        }
//
//        /**
//         * Gets this entry priority
//         *
//         * @return The priority
//         */
//        public int getPriority() {
//            return priority;
//        }
//
//        /**
//         * Gets the executor
//         *
//         * @return The executor
//         */
//        @NotNull
//        public Function<Map.Entry<@NotNull CommandSender, @NotNull Shop>, @Nullable QuickComponent> getExecutor() {
//            return executor;
//        }
//
//        /**
//         * Execute the executor and gets an QuickComponent (or null for no out-put)
//         *
//         * @param data The input data that is an entry contains sender and shop object
//         * @return The QuickComponent or null for no out-put
//         */
//        @Nullable
//        public QuickComponent get(@NotNull Map.Entry<@NotNull CommandSender, @NotNull Shop> data) {
//            return this.executor.apply(data);
//        }
//    }
//
//    public static class Comparator implements java.util.Comparator<Entry>, Serializable {
//
//        /**
//         * Compares its two arguments for order.  Returns a negative integer,
//         * zero, or a positive integer as the first argument is less than, equal
//         * to, or greater than the second.<p>
//         * <p>
//         * The implementor must ensure that {@code sgn(compare(x, y)) ==
//         * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
//         * implies that {@code compare(x, y)} must throw an exception if and only
//         * if {@code compare(y, x)} throws an exception.)<p>
//         * <p>
//         * The implementor must also ensure that the relation is transitive:
//         * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
//         * {@code compare(x, z)>0}.<p>
//         * <p>
//         * Finally, the implementor must ensure that {@code compare(x, y)==0}
//         * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
//         * {@code z}.<p>
//         * <p>
//         * It is generally the case, but <i>not</i> strictly required that
//         * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
//         * any comparator that violates this condition should clearly indicate
//         * this fact.  The recommended language is "Note: this comparator
//         * imposes orderings that are inconsistent with equals."<p>
//         * <p>
//         * In the foregoing description, the notation
//         * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
//         * <i>signum</i> function, which is defined to return one of {@code -1},
//         * {@code 0}, or {@code 1} according to whether the value of
//         * <i>expression</i> is negative, zero, or positive, respectively.
//         *
//         * @param o1 the first object to be compared.
//         * @param o2 the second object to be compared.
//         * @return a negative integer, zero, or a positive integer as the
//         * first argument is less than, equal to, or greater than the
//         * second.
//         * @throws NullPointerException if an argument is null and this
//         *                              comparator does not permit null arguments
//         * @throws ClassCastException   if the arguments' types prevent them from
//         *                              being compared by this comparator.
//         */
//        @Override
//        public int compare(Entry o1, Entry o2) {
//            return o1.priority - o2.priority;
//        }
//    }
//}

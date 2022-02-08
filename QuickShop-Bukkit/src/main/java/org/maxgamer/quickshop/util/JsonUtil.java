/*
 * This file is a part of project QuickShop, the name is JsonUtil.java
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

package org.maxgamer.quickshop.util;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.util.Objects;


/**
 * Utilities to prevent create Gson object in other place and reuse gson object in runtime
 *
 * @author Ghost_chu and sandtechnology, modified based on Lucko's Helper project
 */
public final class JsonUtil {
    private static final Gson REGULAR_GSON = new Gson();
    private static final Gson STANDARD_GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINT_GSON = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(new HiddenAnnotationExclusionStrategy())
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final JsonParser PARSER = new JsonParser();

    public static Gson regular() {
        return REGULAR_GSON;
    }

    @NotNull
    public static Gson standard() {
        return STANDARD_GSON;
    }

    public static Gson getGson() {
        return STANDARD_GSON;
    }

    @NotNull
    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    @NotNull
    public static JsonParser parser() {
        return PARSER;
    }

    @NotNull
    public static JsonObject readObject(@NotNull Reader reader) {
        return PARSER.parse(reader).getAsJsonObject();
    }

    @NotNull
    public static JsonObject readObject(@NotNull String s) {
        return PARSER.parse(s).getAsJsonObject();
    }

    public static void writeObject(@NotNull Appendable writer, @NotNull JsonObject object) {
        standard().toJson(object, writer);
    }

    public static void writeObjectPretty(@NotNull Appendable writer, @NotNull JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    public static void writeElement(@NotNull Appendable writer, @NotNull JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(@NotNull Appendable writer, @NotNull JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    @NotNull
    public static String toString(@NotNull JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    @NotNull
    public static String toStringPretty(@NotNull JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }


    @NotNull
    @Deprecated
    public static Gson get() {
        return standard();
    }

    @NotNull
    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

    public @interface Hidden {

    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getDeclaredAnnotation(Hidden.class) != null;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Hidden.class) != null;
        }
    }

}

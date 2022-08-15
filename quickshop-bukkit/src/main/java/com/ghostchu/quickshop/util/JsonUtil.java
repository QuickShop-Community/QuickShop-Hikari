package com.ghostchu.quickshop.util;

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

    @NotNull
    @Deprecated
    public static Gson get() {
        return standard();
    }

    public static Gson getGson() {
        return STANDARD_GSON;
    }

    @NotNull
    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

    @NotNull
    public static JsonObject readObject(@NotNull Reader reader) {
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    @NotNull
    public static JsonObject readObject(@NotNull String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    public static Gson regular() {
        return REGULAR_GSON;
    }

    @NotNull
    public static String toString(@NotNull JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    @NotNull
    public static String toStringPretty(@NotNull JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }

    public static void writeElement(@NotNull Appendable writer, @NotNull JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(@NotNull Appendable writer, @NotNull JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    public static void writeObject(@NotNull Appendable writer, @NotNull JsonObject object) {
        standard().toJson(object, writer);
    }

    @NotNull
    public static Gson standard() {
        return STANDARD_GSON;
    }

    public static void writeObjectPretty(@NotNull Appendable writer, @NotNull JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    @NotNull
    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    public @interface Hidden {

    }

    public static class HiddenAnnotationExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(Hidden.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return clazz.getDeclaredAnnotation(Hidden.class) != null;
        }
    }

}

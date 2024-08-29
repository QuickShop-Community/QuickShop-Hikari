package com.ghostchu.quickshop.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.UUID;

public class CommonUtil {
    private CommonUtil() {
    }

    /**
     * Convert strArray to String. E.g "Foo, Bar"
     *
     * @param strArray Target array
     * @return str
     */
    @NotNull
    public static String array2String(@NotNull String[] strArray) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String str : strArray) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    public static double min(@NotNull List<Double> total){
        double min = Double.MAX_VALUE;
        for (Double i : total) {
            min = Math.min(i, min);
        }
        return  min;
    }

    public static double max(@NotNull List<Double> total){
        double max = Double.MIN_VALUE;
        for (Double i : total) {
            max = Math.max(i, max);
        }
        return max;
    }

    public static double avg(@NotNull List<Double> total){
        double t = 0;
        for (Double v : total) {
            t += v;
        }
        return t / total.size();
    }

    public static double med(@NotNull List<Double> total) {
        total = new ArrayList<>(total);
        double j;
        Collections.sort(total);
        int size = total.size();
        if (size % 2 == 1) {
            j = total.get((size - 1) / 2);
        } else {
            j = (total.get(size / 2 - 1) + total.get(size / 2) + 0.0) / 2;
        }
        return j;
    }

    @Nullable
    public static Date parseTime(@NotNull String time) {
        if (NumberUtils.isCreatable(time)) {
            return new Date(Long.parseLong(time) * 1000L);
        }
        return zuluTime2Date(time);
    }

    @Nullable
    public static Date zuluTime2Date(@NotNull String zuluString) {
        String pattern = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.getPattern();
        try {
            return DateUtils.parseDate(zuluString, pattern);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert boolean to string status
     *
     * @param bool Boolean
     * @return Enabled or Disabled
     */
    @NotNull
    public static String boolean2Status(boolean bool) {
        if (bool) {
            return "Enabled";
        } else {
            return "Disabled";
        }
    }

    /**
     * Create regex from glob
     *
     * @param glob glob
     * @return regex
     */
    // https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
    @NotNull
    public static String createRegexFromGlob(@NotNull String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*' -> out.append(".*");
                case '?' -> out.append('.');
                case '.' -> out.append("\\.");
                case '\\' -> out.append("\\\\");
                default -> out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }

    public static boolean deleteDirectory(@NotNull File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return false;
            }
            for (String child : children) {
                if (!deleteDirectory(new File(dir, child))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Gets the location of a class inside of a jar file.
     *
     * @param clazz The class to get the location of.
     * @return The jar path which given class at.
     */
    @NotNull
    public static String getClassPathRelative(@NotNull Class<?> clazz) {
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        File file = new File(jarPath);
        return getRelativePath(new File("."), file);
    }

    @NotNull
    public static String getRelativePath(@NotNull File rootPath, @NotNull File targetPath) {
        try {
            return rootPath.toURI().relativize(targetPath.toURI()).getPath();
        } catch (Exception e) {
            return targetPath.getAbsolutePath();
        }
    }

    /**
     * Return the Class name.
     *
     * @param c The class to get name
     * @return The class prefix
     */
    @NotNull
    public static String getClassPrefix(@NotNull Class<?> c) {
        String callClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String customClassName = c.getSimpleName();
        return "[" + callClassName + "-" + customClassName + "] ";
    }

    /**
     * Return the Class name.
     *
     * @return The class prefix
     */
    @NotNull
    public static String getClassPrefix() {
        String className = Thread.currentThread().getStackTrace()[2].getClassName();
        try {
            Class<?> c = Class.forName(className);
            className = c.getSimpleName();
            if (!c.getSimpleName().isEmpty()) {
                className = c.getSimpleName();
            }
        } catch (ClassNotFoundException ignored) {
        }
        String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
        return "[" + className + "-" + methodName + "] ";
    }

    /**
     * Convert timestamp to LocalDate instance
     *
     * @param timestamp Timestamp
     * @return LocalDate instance
     */
    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    @Nullable
    public static LocalDate getDateFromTimestamp(long timestamp) {
        LocalDateTime date = getDateTimeFromTimestamp(timestamp);
        return date == null ? null : date.toLocalDate();
    }

    /**
     * Convert timestamp to LocalDateTime instance
     *
     * @param timestamp Timestamp
     * @return LocalDateTime instance
     */
    // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
    public static @NotNull LocalDateTime getDateTimeFromTimestamp(long timestamp) {
        if (timestamp == 0) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(0), TimeZone
                    .getDefault().toZoneId());
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), TimeZone
                .getDefault().toZoneId());
    }

    /**
     * Gets the nil unique id
     *
     * @return uuid which content is `00000000-0000-0000-0000-000000000000`
     */
    @NotNull
    public static UUID getNilUniqueId() {
        return new UUID(0, 0);
    }

    @NotNull
    public static String getRelativePath(@NotNull File targetPath) {
        try {
            return new File(".").toURI().relativize(targetPath.toURI()).getPath();
        } catch (Exception e) {
            return targetPath.getAbsolutePath();
        }
    }

    /**
     * Read the InputStream to the byte array.
     *
     * @param filePath Target file
     * @return Byte array
     */
    public static byte[] inputStream2ByteArray(@NotNull String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            return toByteArray(in);
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static byte[] toByteArray(@NotNull InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * Get this class available or not
     *
     * @param qualifiedName class qualifiedName
     * @return boolean Available
     */
    public static boolean isClassAvailable(@NotNull String qualifiedName) {
        try {
            Class.forName(qualifiedName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Check a string is or not a UUID string
     *
     * @param string Target string
     * @return is UUID
     */
    public static boolean isUUID(@NotNull String string) {
        final int length = string.length();
        if (length != 36 && length != 32) {
            return false;
        }
        final String[] components = string.split("-");
        return components.length == 5;
    }

    public static boolean isTrimmedUUID(@NotNull String string) {
        try {
            fromTrimmedUUID(string);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    public static UUID fromTrimmedUUID(@NotNull String trimmedUUID) {
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        /* Backwards adding to avoid index adjustments */
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        return UUID.fromString(builder.toString());
    }

    public static int multiProcessorThreadRecommended() {
        int processors = Runtime.getRuntime().availableProcessors();
        if (processors >= 2) {
            processors--;
        }
        return processors;
    }


    public static boolean isJson(String str) {
        if (str == null || str.isBlank()) {
            return false;
        }
        try {
            JsonElement element = JsonParser.parseString(str);
            return element.isJsonObject() || element.isJsonArray();
        } catch (JsonParseException exception) {
            return false;
        }
    }

    @SafeVarargs
    @NotNull
    public static <T> List<T> linkLists(List<T>... lists) {
        List<T> fList = new ArrayList<>();
        for (List<T> objList : lists) {
            fList.addAll(objList);
        }
        return fList;
    }

    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2String(@NotNull Collection<?> strList) {
        return String.join(", ", strList.stream().map(Object::toString).toList());
    }

    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2StringBreaks(@NotNull Collection<?> strList) {
        return String.join("\n", strList.stream().map(Object::toString).toList());
    }

    /**
     * Matches the given lists but disordered.
     *
     * @param list1 List1
     * @param list2 List2
     * @return Lists matches or not
     */
    public static boolean listDisorderMatches(@NotNull Collection<?> list1, @NotNull Collection<?> list2) {
        return list1.containsAll(list2) && list2.containsAll(list1);
    }

    /**
     * Merge args array to a String object with space
     *
     * @param args Args
     * @return String object
     */
    @NotNull
    public static String mergeArgs(@NotNull String[] args) {
        StringJoiner joiner = new StringJoiner(" ", "", "");
        for (String arg : args) {
            joiner.add(arg);
        }
        return joiner.toString();
    }

    /**
     * Converts a name like IRON_INGOT into Iron Ingot to improve readability
     *
     * @param ugly The string such as IRON_INGOT
     * @return A nicer version, such as Iron Ingot
     */
    @NotNull
    public static String prettifyText(@NotNull String ugly) {
        String[] nameParts = ugly.split("_");
        if (nameParts.length == 1) {
            return firstUppercase(ugly);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameParts.length; i++) {
            if (!nameParts[i].isEmpty()) {
                sb.append(Character.toUpperCase(nameParts[i].charAt(0))).append(nameParts[i].substring(1).toLowerCase());
            }
            if (i + 1 != nameParts.length) {
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * First uppercase for every words the first char for a text.
     *
     * @param string text
     * @return Processed text.
     */
    @NotNull
    public static String firstUppercase(@NotNull String string) {
        if (string.length() > 1) {
            return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
        } else {
            return string.toUpperCase();
        }
    }

    /**
     * Read the file to the String
     *
     * @param fileName Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull String fileName) {
        File file = new File(fileName);
        return readToString(file);
    }

    /**
     * Read the file to the String
     *
     * @param file Target file.
     * @return Target file's content.
     */
    @NotNull
    public static String readToString(@NotNull File file) {
        byte[] filecontent = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(filecontent, StandardCharsets.UTF_8);
    }

    /**
     * Create a list which only contains the given elements at the tail of a list.
     *
     * @param list List
     * @param last The amount of elements from list tail to be added to the new list
     * @return The new list
     */
    @NotNull
    public static List<String> tail(@NotNull List<String> list, int last) {
        return list.subList(Math.max(list.size() - last, 0), list.size());
    }

    @NotNull
    public static String getTZTimestamp(@NotNull Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(date);
    }

    /**
     * Gets the location of a class inside of a jar file.
     *
     * @param clazz The class to get the location of.
     * @return The jar path which given class at.
     */
    @NotNull
    public static String getClassPath(@NotNull Class<?> clazz) {
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
        return jarPath;
    }
}

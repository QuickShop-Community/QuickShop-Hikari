package com.ghostchu.quickshop.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
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
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
   *
   * @return str
   */
  @NotNull
  public static String array2String(@NotNull final String[] strArray) {

    final StringJoiner joiner = new StringJoiner(", ");
    for(final String str : strArray) {
      joiner.add(str);
    }
    return joiner.toString();
  }

  public static double min(@NotNull final List<Double> total) {

    double min = Double.MAX_VALUE;
    for(final Double i : total) {
      min = Math.min(i, min);
    }
    return min;
  }

  public static double max(@NotNull final List<Double> total) {

    double max = Double.MIN_VALUE;
    for(final Double i : total) {
      max = Math.max(i, max);
    }
    return max;
  }

  public static double avg(@NotNull final List<Double> total) {

    double t = 0;
    for(final Double v : total) {
      t += v;
    }
    return t / total.size();
  }

  public static double med(@NotNull List<Double> total) {

    total = new ArrayList<>(total);
    final double j;
    Collections.sort(total);
    final int size = total.size();
    if(size % 2 == 1) {
      j = total.get((size - 1) / 2);
    } else {
      j = (total.get(size / 2 - 1) + total.get(size / 2) + 0.0) / 2;
    }
    return j;
  }

  @Nullable
  public static Date parseTime(@NotNull final String time) {

    try {
      return new Date(Long.parseLong(time) * 1000L);
    } catch(final NumberFormatException ignore) {

      return parseZuluDate(time);
    }
  }

  @Nullable
  public static Date parseZuluDate(final String zuluString) {

    final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    try {
      final ZonedDateTime zdt = ZonedDateTime.parse(zuluString, formatter);
      return Date.from(zdt.toInstant());
    } catch(final DateTimeParseException e) {
      return null;
    }
  }

  public static boolean isEmptyString(final String check) {

    return check == null || check.isEmpty();
  }

  public static boolean isBlank(final String check) {

    return check == null || check.trim().isEmpty();
  }

  public static boolean strEquals(final String str1, final String str2) {

    if(str1 == null && str2 == null) {
      return true;
    }

    if(str1 == null || str2 == null) {
      return false;
    }
    return str1.equals(str2);
  }

  /**
   * {@return whether the provided sequence consts of only digits}
   * @param sequence The sequence to check
   */
  public static boolean isNumeric(final CharSequence sequence) {

    if(sequence == null || sequence.isEmpty()) {
      return false;
    }

    for(int i = 0; i < sequence.length(); i++) {

      if(Character.isDigit(sequence.charAt(i))) {
        continue;
      }

      return false;
    }
    return true;
  }

  /**
   * {@return whether the provided sequence is a valid non-negative integer}
   * @param sequence The sequence to check
   */
  public static boolean isInteger(final CharSequence sequence) {

    // isNumeric currently returns false for the '-' character, which ensures that this only returns true for non-negative ints.
    if(!isNumeric(sequence)) {
      return false;
    }

    try {
      Integer.parseInt(sequence.toString());
      return true;
    } catch(NumberFormatException e) {
      return false;
    }
  }

  public static String subAfter(final String string, final @NotNull String separator) {

    if(isEmptyString(string)) {
      return string;
    }

    final int pos = string.indexOf(separator);
    if(pos == -1) {
      return "";
    }

    return string.substring(pos + separator.length());
  }

  public static String subBeforeLast(final String string, final String separator) {

    if(isEmptyString(string) || isEmptyString(separator)) {
      return string;
    }

    final int pos = string.lastIndexOf(separator);
    if(pos == -1) {
      return string;
    }
    return string.substring(0, pos);
  }

  public static <T> boolean arrayContains(final T[] array, final T value) {

    if(array == null) {
      return false;
    }

    for(final T element : array) {
      if(Objects.equals(element, value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Convert boolean to string status
   *
   * @param bool Boolean
   *
   * @return Enabled or Disabled
   */
  @NotNull
  public static String boolean2Status(final boolean bool) {

    if(bool) {
      return "Enabled";
    } else {
      return "Disabled";
    }
  }

  /**
   * Create regex from glob
   *
   * @param glob glob
   *
   * @return regex
   */
  // https://stackoverflow.com/questions/45321050/java-string-matching-with-wildcards
  @NotNull
  public static String createRegexFromGlob(@NotNull final String glob) {

    final StringBuilder out = new StringBuilder("^");
    for(int i = 0; i < glob.length(); ++i) {
      final char c = glob.charAt(i);
      switch(c) {
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

  public static boolean deleteDirectory(@NotNull final File dir) {

    if(dir.isDirectory()) {
      final String[] children = dir.list();
      if(children == null) {
        return false;
      }
      for(final String child : children) {
        if(!deleteDirectory(new File(dir, child))) {
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
   *
   * @return The jar path which given class at.
   */
  @NotNull
  public static String getClassPathRelative(@NotNull final Class<?> clazz) {

    String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
    final File file = new File(jarPath);
    return getRelativePath(new File("."), file);
  }

  @NotNull
  public static String getRelativePath(@NotNull final File rootPath, @NotNull final File targetPath) {

    try {
      return rootPath.toURI().relativize(targetPath.toURI()).getPath();
    } catch(final Exception e) {
      return targetPath.getAbsolutePath();
    }
  }

  /**
   * Return the Class name.
   *
   * @param c The class to get name
   *
   * @return The class prefix
   */
  @NotNull
  public static String getClassPrefix(@NotNull final Class<?> c) {

    final String callClassName = Thread.currentThread().getStackTrace()[2].getClassName();
    final String customClassName = c.getSimpleName();
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
      final Class<?> c = Class.forName(className);
      className = c.getSimpleName();
      if(!c.getSimpleName().isEmpty()) {
        className = c.getSimpleName();
      }
    } catch(final ClassNotFoundException ignored) {
    }
    final String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    return "[" + className + "-" + methodName + "] ";
  }

  /**
   * Convert timestamp to LocalDate instance
   *
   * @param timestamp Timestamp
   *
   * @return LocalDate instance
   */
  // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
  @Nullable
  public static LocalDate getDateFromTimestamp(final long timestamp) {

    final LocalDateTime date = getDateTimeFromTimestamp(timestamp);
    return date == null? null : date.toLocalDate();
  }

  /**
   * Convert timestamp to LocalDateTime instance
   *
   * @param timestamp Timestamp
   *
   * @return LocalDateTime instance
   */
  // http://www.java2s.com/Tutorials/Java/Data_Type_How_to/Date_Convert/Convert_long_type_timestamp_to_LocalDate_and_LocalDateTime.htm
  public static @NotNull LocalDateTime getDateTimeFromTimestamp(final long timestamp) {

    if(timestamp == 0) {
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
  public static String getRelativePath(@NotNull final File targetPath) {

    try {
      return new File(".").toURI().relativize(targetPath.toURI()).getPath();
    } catch(final Exception e) {
      return targetPath.getAbsolutePath();
    }
  }

  /**
   * Read the InputStream to the byte array.
   *
   * @param filePath Target file
   *
   * @return Byte array
   */
  public static byte[] inputStream2ByteArray(@NotNull final String filePath) {

    try(final InputStream in = new FileInputStream(filePath)) {
      return toByteArray(in);
    } catch(final IOException e) {
      return new byte[0];
    }
  }

  private static byte[] toByteArray(@NotNull final InputStream in) throws IOException {

    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final byte[] buffer = new byte[1024 * 4];
    int n;
    while((n = in.read(buffer)) != -1) {
      out.write(buffer, 0, n);
    }
    return out.toByteArray();
  }

  /**
   * Get this class available or not
   *
   * @param qualifiedName class qualifiedName
   *
   * @return boolean Available
   */
  public static boolean isClassAvailable(@NotNull final String qualifiedName) {

    try {
      Class.forName(qualifiedName);
      return true;
    } catch(final ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Check a string is or not a UUID string
   *
   * @param string Target string
   *
   * @return is UUID
   */
  public static boolean isUUID(@NotNull final String string) {

    final int length = string.length();
    if(length != 36 && length != 32) {
      return false;
    }
    final String[] components = string.split("-");
    return components.length == 5;
  }

  public static boolean isTrimmedUUID(@NotNull final String string) {

    try {
      fromTrimmedUUID(string);
      return true;
    } catch(final IllegalArgumentException e) {
      return false;
    }
  }


  public static UUID fromTrimmedUUID(@NotNull final String trimmedUUID) {

    final StringBuilder builder = new StringBuilder(trimmedUUID.trim());
    /* Backwards adding to avoid index adjustments */
    try {
      builder.insert(20, "-");
      builder.insert(16, "-");
      builder.insert(12, "-");
      builder.insert(8, "-");
    } catch(final StringIndexOutOfBoundsException e) {
      throw new IllegalArgumentException();
    }

    return UUID.fromString(builder.toString());
  }

  public static int multiProcessorThreadRecommended() {

    int processors = Runtime.getRuntime().availableProcessors();
    if(processors >= 2) {
      processors--;
    }
    return processors;
  }


  public static boolean isJson(final String str) {

    if(str == null || str.isBlank()) {
      return false;
    }
    try {
      final JsonElement element = JsonParser.parseString(str);
      return element.isJsonObject() || element.isJsonArray();
    } catch(final JsonParseException exception) {
      return false;
    }
  }

  @SafeVarargs
  @NotNull
  public static <T> List<T> linkLists(final List<T>... lists) {

    final List<T> fList = new ArrayList<>();
    for(final List<T> objList : lists) {
      fList.addAll(objList);
    }
    return fList;
  }

  /**
   * Convert strList to String. E.g "Foo, Bar"
   *
   * @param strList Target list
   *
   * @return str
   */
  @NotNull
  public static String list2String(@NotNull final Collection<?> strList) {

    return String.join(", ", strList.stream().map(Object::toString).toList());
  }

  /**
   * Convert strList to String. E.g "Foo, Bar"
   *
   * @param strList Target list
   *
   * @return str
   */
  @NotNull
  public static String list2StringBreaks(@NotNull final Collection<?> strList) {

    return String.join("\n", strList.stream().map(Object::toString).toList());
  }

  /**
   * Matches the given lists but disordered.
   *
   * @param list1 List1
   * @param list2 List2
   *
   * @return Lists matches or not
   */
  public static boolean listDisorderMatches(@NotNull final Collection<?> list1, @NotNull final Collection<?> list2) {

    return list1.containsAll(list2) && list2.containsAll(list1);
  }

  /**
   * Merge args array to a String object with space
   *
   * @param args Args
   *
   * @return String object
   */
  @NotNull
  public static String mergeArgs(@NotNull final String[] args) {

    final StringJoiner joiner = new StringJoiner(" ", "", "");
    for(final String arg : args) {
      joiner.add(arg);
    }
    return joiner.toString();
  }

  /**
   * Converts a name like IRON_INGOT into Iron Ingot to improve readability
   *
   * @param ugly The string such as IRON_INGOT
   *
   * @return A nicer version, such as Iron Ingot
   */
  @NotNull
  public static String prettifyText(@NotNull final String ugly) {

    final String[] nameParts = ugly.split("_");
    if(nameParts.length == 1) {
      return firstUppercase(ugly);
    }
    final StringBuilder sb = new StringBuilder();
    for(int i = 0; i < nameParts.length; i++) {
      if(!nameParts[i].isEmpty()) {
        sb.append(Character.toUpperCase(nameParts[i].charAt(0))).append(nameParts[i].substring(1).toLowerCase());
      }
      if(i + 1 != nameParts.length) {
        sb.append(" ");
      }
    }

    return sb.toString();
  }

  /**
   * First uppercase for every words the first char for a text.
   *
   * @param string text
   *
   * @return Processed text.
   */
  @NotNull
  public static String firstUppercase(@NotNull final String string) {

    if(string.length() > 1) {
      return Character.toUpperCase(string.charAt(0)) + string.substring(1).toLowerCase();
    } else {
      return string.toUpperCase();
    }
  }

  /**
   * Read the file to the String
   *
   * @param fileName Target file.
   *
   * @return Target file's content.
   */
  @NotNull
  public static String readToString(@NotNull final String fileName) {

    final File file = new File(fileName);
    return readToString(file);
  }

  /**
   * Read the file to the String
   *
   * @param file Target file.
   *
   * @return Target file's content.
   */
  @NotNull
  public static String readToString(@NotNull final File file) {

    final byte[] filecontent = new byte[(int)file.length()];
    try(final FileInputStream in = new FileInputStream(file)) {
      in.read(filecontent);
    } catch(final IOException e) {
      e.printStackTrace();
    }
    return new String(filecontent, StandardCharsets.UTF_8);
  }

  /**
   * Create a list which only contains the given elements at the tail of a list.
   *
   * @param list List
   * @param last The amount of elements from list tail to be added to the new list
   *
   * @return The new list
   */
  @NotNull
  public static List<String> tail(@NotNull final List<String> list, final int last) {

    return list.subList(Math.max(list.size() - last, 0), list.size());
  }

  @NotNull
  public static String getTZTimestamp(@NotNull final Date date) {

    final TimeZone tz = TimeZone.getTimeZone("UTC");
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    return df.format(date);
  }

  /**
   * Gets the location of a class inside of a jar file.
   *
   * @param clazz The class to get the location of.
   *
   * @return The jar path which given class at.
   */
  @NotNull
  public static String getClassPath(@NotNull final Class<?> clazz) {

    String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
    return jarPath;
  }
}

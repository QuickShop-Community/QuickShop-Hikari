package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

public class PackageUtil {

  /**
   * Parse the given name with package.class prefix (all-lowercases) from property
   *
   * @param name name
   *
   * @return ParseResult
   */
  @NotNull
  public static PackageUtil.SysPropertiesParseResult parsePackageProperly(@NotNull final String name) {

    final Log.Caller caller = Log.Caller.createSync(false);
    final String str = caller.getClassName() + "." + name;
    final String value = System.getProperty(str);
    return new SysPropertiesParseResult(str, value);
  }

  @Data
  public static class SysPropertiesParseResult {

    private final String key;
    private final String value;

    public SysPropertiesParseResult(final String key, final String value) {

      this.key = key;
      this.value = value;
    }

    public boolean asBoolean() {

      return Boolean.parseBoolean(value);
    }

    public boolean asBoolean(final boolean def) {

      if(value == null) {
        return def;
      }
      return Boolean.parseBoolean(value);
    }

    public byte asByte(final byte def) {

      if(value == null) {
        return def;
      }
      try {
        return Byte.parseByte(value);
      } catch(NumberFormatException exception) {
        return def;
      }
    }

    public double asDouble(final double def) {

      if(value == null) {
        return def;
      }
      try {
        return Double.parseDouble(value);
      } catch(NumberFormatException exception) {
        return def;
      }
    }

    public int asInteger(final int def) {

      if(value == null) {
        return def;
      }
      try {
        return Integer.parseInt(value);
      } catch(NumberFormatException exception) {
        return def;
      }
    }

    public long asLong(final long def) {

      if(value == null) {
        return def;
      }
      try {
        return Long.parseLong(value);
      } catch(NumberFormatException exception) {
        return def;
      }
    }

    public short asShort(final short def) {

      if(value == null) {
        return def;
      }
      try {
        return Short.parseShort(value);
      } catch(NumberFormatException exception) {
        return def;
      }
    }

    @NotNull
    public String asString(@NotNull final String def) {

      if(value == null) {
        return def;
      }
      return value;
    }

    @NotNull
    public String getParseKey() {

      return key;
    }

    public boolean isPresent() {

      return value != null;
    }
  }
}

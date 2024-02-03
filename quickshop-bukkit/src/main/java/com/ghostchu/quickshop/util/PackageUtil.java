package com.ghostchu.quickshop.util;

import com.ghostchu.quickshop.util.logger.Log;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

public class PackageUtil {
    /**
     * Parse the given name with package.class prefix (all-lowercases) from property
     *
     * @param name name
     * @return ParseResult
     */
    @NotNull
    public static PackageUtil.SysPropertiesParseResult parsePackageProperly(@NotNull String name) {
        Log.Caller caller = Log.Caller.createSync(false);
        String str = caller.getClassName() + "." + name;
        String value = System.getProperty(str);
        return new SysPropertiesParseResult(str, value);
    }

    @Data
    public static class SysPropertiesParseResult {
        private final String key;
        private final String value;

        public SysPropertiesParseResult(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(value);
        }

        public boolean asBoolean(boolean def) {
            if (value == null) {
                return def;
            }
            return Boolean.parseBoolean(value);
        }

        public byte asByte(byte def) {
            if (value == null) {
                return def;
            }
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public double asDouble(double def) {
            if (value == null) {
                return def;
            }
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public int asInteger(int def) {
            if (value == null) {
                return def;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public long asLong(long def) {
            if (value == null) {
                return def;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        public short asShort(short def) {
            if (value == null) {
                return def;
            }
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException exception) {
                return def;
            }
        }

        @NotNull
        public String asString(@NotNull String def) {
            if (value == null) {
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

package org.maxgamer.quickshop.database;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataType {
    @Getter
    public DataTypeMapping datatype;
    @Getter
    public Integer length;
    @Getter
    public Object defaultValue;

    public DataType(@NotNull DataTypeMapping type, @Nullable Integer length, @NotNull Object defaultValue) {
        this.datatype = type;
        if (length != null) {
            Validate.isTrue(length > 1, "Field length cannot be negative or zero.");
        }
        this.length = length;
        this.defaultValue = defaultValue;
    }

    public DataType(@NotNull DataTypeMapping type, @Nullable Integer length) {
        this.datatype = type;
        if (length != null) {
            Validate.isTrue(length > 1, "Field length cannot be negative or zero.");
        }
        this.length = length;
        this.defaultValue = null;
    }

    public DataType(@NotNull DataTypeMapping type, @NotNull Object defaultValue) {
        this.datatype = type;
        this.length = null;
        this.defaultValue = defaultValue;
    }

    public DataType(@NotNull DataTypeMapping type) {
        this.datatype = type;
        this.length = null;
    }

}

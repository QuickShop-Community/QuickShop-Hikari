package com.ghostchu.quickshop.mock;

import lombok.ToString;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

@ToString
public class MckFileConfiguration extends FileConfiguration {
    @NotNull
    @Override
    public String saveToString() {
        return "";
    }

    @Override
    public void loadFromString(@NotNull String s) {
    }

    @NotNull
    @Override
    protected String buildHeader() {
        return "";
    }

}

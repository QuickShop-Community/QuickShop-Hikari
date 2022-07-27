package com.ghostchu.quickshop.api.localization.text;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class ProxiedLocale {
    @Nullable
    private String origin;
    private String relative;

    public ProxiedLocale(@Nullable String origin, String relative) {
        this.origin = origin;
        this.relative = relative;
    }

    public String getLocale() {
        return relative;
    }
}

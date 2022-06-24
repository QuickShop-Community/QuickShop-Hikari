package com.ghostchu.quickshop.api.localization.text;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Data
public class ProxiedLocale {
    @Nullable
    private String origin;
    private String relative;

    public String getLocale() {
        return relative;
    }
}

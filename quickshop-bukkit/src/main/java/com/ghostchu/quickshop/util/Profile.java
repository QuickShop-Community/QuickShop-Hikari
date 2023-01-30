package com.ghostchu.quickshop.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class Profile {
    private final UUID uniqueId;
    private final String name;
}

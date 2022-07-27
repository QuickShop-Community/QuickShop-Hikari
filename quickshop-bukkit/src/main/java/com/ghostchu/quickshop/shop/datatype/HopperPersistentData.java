package com.ghostchu.quickshop.shop.datatype;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class HopperPersistentData {
    @Expose
    private final UUID player;
}

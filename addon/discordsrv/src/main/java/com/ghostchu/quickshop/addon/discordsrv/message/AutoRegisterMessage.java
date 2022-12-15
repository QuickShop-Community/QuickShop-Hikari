package com.ghostchu.quickshop.addon.discordsrv.message;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface AutoRegisterMessage {
    @Nullable String key();
}

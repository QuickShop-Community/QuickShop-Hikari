package com.ghostchu.quickshop.util.config;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as update script and will be executed by ConfigurationUpdater
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface UpdateScript {
    @Nullable String description() default "";

    int version();
}

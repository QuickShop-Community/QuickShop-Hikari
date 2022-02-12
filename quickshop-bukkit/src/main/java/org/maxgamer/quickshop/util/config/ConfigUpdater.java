package org.maxgamer.quickshop.util.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface ConfigUpdater {
    int version();
}

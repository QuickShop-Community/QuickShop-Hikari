package com.ghostchu.quickshop.util.envcheck;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface EnvCheckEntry {
    String name() default "Unknown EnvCheck Test";

    int priority() default 50;

    Stage[] stage() default Stage.ON_ENABLE;

    enum Stage {
        CONSTRUCTOR,
        ON_LOAD,
        ON_ENABLE,
        AFTER_ON_ENABLE
    }
}

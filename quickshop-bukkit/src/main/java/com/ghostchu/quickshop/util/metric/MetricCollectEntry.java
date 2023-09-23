package com.ghostchu.quickshop.util.metric;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface MetricCollectEntry {
    MetricDataType dataType();

    String moduleName();

    String description();
}

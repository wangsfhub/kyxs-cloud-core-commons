package com.kyxs.cloud.core.base.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wangsf
 * @since 2023/2/25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RepeatSubmit {
    // redis的key
    String prefix() default "resubmitCheckKey::";
    //过期时间
    int expire() default 10;
    //时间单位
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

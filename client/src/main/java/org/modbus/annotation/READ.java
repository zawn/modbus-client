package org.modbus.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author zhangzhenli
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface READ {

    int start() default 0;

    int end() default 0;

    int quantity() default 0;
}

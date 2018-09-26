package org.modbus.annotation;

/**
 * @author zhangzhenli
 */
public @interface READ {

    int start() default 0;

    int end() default 0;

    int quantity() default 0;
}

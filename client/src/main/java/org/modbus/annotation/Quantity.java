package org.modbus.annotation;

/**
 * @author zhangzhenli
 */
public @interface Quantity {
    int value() default 1;
}

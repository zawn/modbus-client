package org.modbus.converter;

import com.fasterxml.jackson.databind.JavaType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.modbus.Converter;
import org.modbus.Retrofit;
import org.modbus.annotation.Quantity;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;
import org.modbus.jackson.ModbusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.annotation.AnnotationParser;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public final class ModbusConverterFactory extends Converter.Factory {
    private static final Logger logger = LoggerFactory.getLogger(ModbusConverterFactory.class);

    private static final Class<?>[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
            float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
            Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };

    private static boolean isPrimitiveOrString(Class<?> target) {
        if (String.class.isAssignableFrom(target)) {
            return true;
        }

        for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
            if (standardPrimitive.isAssignableFrom(target)) {
                return true;
            }
        }
        return false;
    }

    ModbusMapper modbusMapper = new ModbusMapper();

    @Nullable
    @Override
    public Converter<ByteBuf, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit) {
        return new ModbusResponseBodyConverter(type, annotations);
    }

    @Nullable
    @Override
    public Converter<?, ByteBuf> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit) {
        Quantity quantityAnnotation = null;
        for (Annotation annotation : parameterAnnotations) {
            if (annotation instanceof Quantity) {
                quantityAnnotation = (Quantity) annotation;
            }
        }
        if (quantityAnnotation == null) {
            for (Annotation annotation : methodAnnotations) {
                logger.debug(annotation.annotationType().toString());
                int quantity = 0;
                if (annotation instanceof WRITE) {
                    int start = ((WRITE) annotation).start();
                    quantity = ((WRITE) annotation).quantity();
                    int end = ((WRITE) annotation).end();
                    if (quantity == 0 && end != 0) {
                        quantity = end - start + 1;
                    }
                } else if (annotation instanceof READ) {
                    int start = ((READ) annotation).start();
                    quantity = ((READ) annotation).quantity();
                    int end = ((READ) annotation).end();
                    if (quantity == 0 && end != 0) {
                        quantity = end - start + 1;
                    }
                }
                final int finalQuantity = quantity;
                quantityAnnotation = new Quantity() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }

                    @Override
                    public int value() {
                        return finalQuantity;
                    }
                };
                parameterAnnotations = new Annotation[parameterAnnotations.length + 1];
                parameterAnnotations[parameterAnnotations.length - 1] = quantityAnnotation;
            }

        }
        logger.warn("ss");

        if (type instanceof Class){
            boolean b = isPrimitiveOrString((Class<?>) type);
            logger.warn("ss");



        }

        return new ModbusRequestBodyConverter(type, parameterAnnotations);
    }

    final class ModbusRequestBodyConverter<T> implements Converter<T, ByteBuf> {

        private final Type type;
        private final Annotation[] parameterAnnotations;


        public ModbusRequestBodyConverter(Type type, Annotation[] parameterAnnotations) {
            this.type = type;
            this.parameterAnnotations = parameterAnnotations;
        }

        @Override
        public ByteBuf convert(T value) throws IOException {


            byte[] bytes = modbusMapper.writeValueAsBytes(value);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            return byteBuf;
        }
    }

    final class ModbusResponseBodyConverter<T> implements Converter<ByteBuf, T> {

        private final JavaType javaType;

        public ModbusResponseBodyConverter(Type type,
                                           Annotation[] annotations) {
            javaType = modbusMapper.getTypeFactory().constructType(type);
        }

        @Override
        public T convert(ByteBuf value) throws IOException {
            byte[] bytes = new byte[value.readableBytes()];
            value.readBytes(bytes);
            return modbusMapper.readValue(bytes, javaType);
        }
    }
}

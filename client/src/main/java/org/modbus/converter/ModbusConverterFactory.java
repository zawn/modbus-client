package org.modbus.converter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.modbus.Converter;
import org.modbus.Retrofit;
import org.modbus.jackson.ModbusMapper;

import com.fasterxml.jackson.databind.JavaType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class ModbusConverterFactory extends Converter.Factory {

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

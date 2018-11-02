package org.modbus.converter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.modbus.Converter;
import org.modbus.jackson.ModbusMapper;

import com.fasterxml.jackson.databind.JavaType;

import io.netty.buffer.ByteBuf;

final class ModbusResponseBodyConverter<T> implements Converter<ByteBuf, T> {

    private final JavaType javaType;
    ModbusMapper modbusMapper = new ModbusMapper();

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


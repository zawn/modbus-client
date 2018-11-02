package org.modbus.converter;

import java.io.IOException;

import org.modbus.Converter;
import org.modbus.jackson.ModbusMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

final class ModbusRequestBodyConverter<T> implements Converter<T, ByteBuf> {

    ModbusMapper modbusMapper = new ModbusMapper();

    @Override
    public ByteBuf convert(T value) throws IOException {
        byte[] bytes = modbusMapper.writeValueAsBytes(value);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        return byteBuf;
    }
}

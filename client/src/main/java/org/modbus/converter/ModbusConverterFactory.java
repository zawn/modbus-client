package org.modbus.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.modbus.Converter;
import org.modbus.Retrofit;

import io.netty.buffer.ByteBuf;

public final class ModbusConverterFactory extends Converter.Factory {

    @Nullable
    @Override
    public Converter<ByteBuf, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {

        return new ModbusResponseBodyConverter(type, annotations);
    }

    @Nullable
    @Override
    public Converter<?, ByteBuf> requestBodyConverter(Type type, Annotation[] parameterAnnotations,
                                                      Annotation[] methodAnnotations,
                                                      Retrofit retrofit) {
        return new ModbusRequestBodyConverter();
    }
}

package org.modbus.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

/**
 * @author zhangzhenli
 */
public class ModbusMapper extends ObjectMapper {

    public ModbusMapper() {
        this(new ModbusFactory(null));
    }

    public ModbusMapper(ModbusFactory modbusFactory) {
        super(modbusFactory);
        registerModule(new ModbusModule());
    }

    public ModbusMapper(ModbusMapper src) {
        super(src);
    }

    public ModbusMapper(ModbusFactory jf,
                        DefaultSerializerProvider sp,
                        DefaultDeserializationContext dc) {
        super(jf, sp, dc);
        registerModule(new ModbusModule());
    }

    @Override
    public ModbusFactory getFactory() {
        return (ModbusFactory) super.getFactory();
    }

    @Override
    public <T> T readValue(byte[] src,
                           Class<T> valueType) throws IOException, JsonParseException, JsonMappingException {
        return super.readValue(src, valueType);
    }

    @Override
    protected Object _readMapAndClose(JsonParser p0, JavaType valueType) throws IOException {
        ((ModbusParser)p0).setJavaType(valueType);
        return super._readMapAndClose(p0, valueType);
    }
}

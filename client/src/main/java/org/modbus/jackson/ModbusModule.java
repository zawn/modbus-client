package org.modbus.jackson;

import java.io.File;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * @author zhangzhenli
 */
public class ModbusModule   extends SimpleModule {

    protected final static ModbusAnnotationIntrospector INTR
            = new ModbusAnnotationIntrospector();

    /**
     * @since 2.9
     */
    protected AnnotationIntrospector _intr = INTR;

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        _addIntrospector(context);
        _addModifiers(context);
        _addDeserializers(context);
//        _addSerializers(context);
    }

    // since 2.9
    protected void _addIntrospector(SetupContext context) {
        if (_intr != null) {
            // insert (instead of append) to have higher precedence
            context.insertAnnotationIntrospector(_intr);
        }
    }

    // since 2.9
    protected void _addModifiers(SetupContext context) {
        // 08-Mar-2016, tatu: to fix [dataformat-avro#35], need to prune 'schema' property:
//        context.addBeanSerializerModifier(new AvroSerializerModifier());
    }

    // since 2.9
    protected void _addDeserializers(SetupContext context) {
        // Override untyped deserializer to one that checks for type information in the schema before going to default handling
        SimpleDeserializers desers = new SimpleDeserializers();
//        desers.addDeserializer(Object.class, new AvroUntypedDeserializer());
        context.addDeserializers(desers);
    }

    // since 2.9
    protected void _addSerializers(SetupContext context) {
        SimpleSerializers sers = new SimpleSerializers();
        // 09-Mar-2017, tatu: As per [dataformats-binary#57], require simple serialization?
//        sers.addSerializer(File.class, new ToStringSerializer(File.class));
        context.addSerializers(sers);
    }
}

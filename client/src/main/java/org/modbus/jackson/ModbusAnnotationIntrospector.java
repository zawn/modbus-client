package org.modbus.jackson;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.modbus.annotation.CharsetName;
import org.modbus.annotation.Quantity;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * @author zhangzhenli
 */
public class ModbusAnnotationIntrospector extends NopAnnotationIntrospector {

    @Override
    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    @Override
    public Object findSerializer(Annotated am) {
        Quantity quantity = am.getAnnotation(Quantity.class);
        CharsetName charsetName = am.getAnnotation(CharsetName.class);
        if (quantity != null) {
            return new StdDelegatingSerializer(
                    new FixLengthByteArrayConverter(quantity, charsetName));
        }
        return super.findSerializer(am);
    }

    /**
     * @author zhangzhenli
     */
    public static class FixLengthByteArrayConverter implements Converter<Object, byte[]> {
        public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-16BE");
        private int quantity;
        private Charset charset;

        public FixLengthByteArrayConverter(BeanProperty property) {
            this(property.getAnnotation(Quantity.class), property.getAnnotation(CharsetName.class));
        }

        public FixLengthByteArrayConverter(int quantity, Charset charset) {
            this.quantity = quantity;
            this.charset = charset;
            if (charset == null)
                this.charset = DEFAULT_CHARSET;
        }

        public FixLengthByteArrayConverter(Quantity q, CharsetName charsetName) {
            quantity = q.value();
            if (charsetName != null) {
                charset = Charset.forName(charsetName.value());
            } else {
                charset = DEFAULT_CHARSET;
            }
        }

        @Override
        public byte[] convert(Object value) {
            if (value == null) {
                return new byte[quantity];
            } else {
                if (value instanceof Number) {
                    return numberToByteArray((Number) value, quantity);
                } else if (value instanceof Boolean) {
                    return booleanToByteArray((Boolean) value, quantity);
                } else if (value instanceof Character) {
                    return characterToByteArray((Character) value, quantity, charset);
                } else if (value instanceof String) {
                    return stringToByteArray((String) value, quantity, charset);
                } else {
                    throw new UnsupportedOperationException("不支持的数据类型," + value.getClass());
                }
            }
        }

        private byte[] stringToByteArray(String value, int quantity, Charset charset) {
            ByteBuffer buffer = ByteBuffer.allocate(quantity);
            buffer.put(value.getBytes(charset));
            return buffer.array();
        }

        private byte[] characterToByteArray(Character value, int quantity, Charset charset) {
            return stringToByteArray(String.valueOf((char) value), quantity, charset);
        }

        private byte[] booleanToByteArray(Boolean value, int quantity) {
            return numberToByteArray(value ? 1 : 0, quantity);
        }

        private byte[] numberToByteArray(Number value, int quantity) {
            //Integer.class, Long.class,Short.class, Float.class, Double.class, Byte.class
            ByteBuffer buffer = ByteBuffer.allocate(quantity);
            if (value instanceof Integer) {
                buffer.putInt((Integer) value);
            } else if (value instanceof Long) {
                buffer.putLong((Long) value);
            } else if (value instanceof Short) {
                buffer.putShort((Short) value);
            } else if (value instanceof Float) {
                buffer.putFloat((Float) value);
            } else if (value instanceof Double) {
                buffer.putDouble((Double) value);
            } else if (value instanceof Byte) {
                buffer.put((Byte) value);
            } else if (value instanceof BigInteger) {
                buffer.put(((BigInteger) value).toByteArray());
            } else {
                throw new UnsupportedOperationException("不支持的数据类型," + value.getClass());
            }
            return buffer.array();
        }

        @Override
        public JavaType getInputType(TypeFactory typeFactory) {
            return typeFactory.constructType(Object.class);
        }

        @Override
        public JavaType getOutputType(TypeFactory typeFactory) {
            return typeFactory.constructArrayType(byte.class);
        }
    }
}

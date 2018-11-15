/*
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package org.modbus.jackson;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.modbus.annotation.CharsetName;
import org.modbus.annotation.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import static org.modbus.jackson.ModbusAnnotationIntrospector.FixLengthByteArrayConverter.DEFAULT_CHARSET;

/**
 */
public class ModbusParser extends ParserMinimalBase {
    private static final Logger logger = LoggerFactory.getLogger(ModbusParser.class);
    private ObjectCodec codec;
    private InputStream in;
    private byte[] inputBuffer;
    private int inputStr;
    private int inputEnd;
    private boolean bufferRecyclable;
    private JavaType javaType;
    private LinkedList<FieldCursor> jsonTokens;
    private Iterator<FieldCursor> jsonTokenIterator;
    private FieldCursor _cursorEntry;
    private FieldCursor _currentFieldCursor;

    public ModbusParser(ObjectCodec codec, InputStream in,
                        byte[] inputBuffer, int inputStr,
                        int inputEnd, boolean bufferRecyclable,
                        JavaType javaType) {

        this.codec = codec;
        this.in = in;
        this.inputBuffer = inputBuffer;
        this.inputStr = inputStr;
        this.inputEnd = inputEnd;
        this.bufferRecyclable = bufferRecyclable;
        this.javaType = javaType;
        if (javaType != null)
            setJavaType(javaType);
    }

    public void setJavaType(JavaType valueType) {
        javaType = valueType;
        jsonTokens = new LinkedList<>();
        flatJavaType(jsonTokens, javaType, 0, inputEnd - inputStr);
        jsonTokenIterator = jsonTokens.iterator();
    }

    private class FieldCursor {
        JsonToken jsonToken;
        AnnotatedField field;
        int inputStr;
        int inputEnd;

        public FieldCursor(JsonToken jsonToken, AnnotatedField field, int inputStr, int inputEnd) {
            this.jsonToken = jsonToken;
            this.field = field;
            this.inputStr = inputStr;
            this.inputEnd = inputEnd;
        }

        public FieldCursor(JsonToken jsonToken) {
            this.jsonToken = jsonToken;
        }

        Charset getCharset() {
            CharsetName charsetName = field.getAnnotation(CharsetName.class);
            Charset charset;
            if (charsetName != null) {
                charset = Charset.forName(charsetName.value());
            } else {
                charset = DEFAULT_CHARSET;
            }
            return charset;
        }
    }


    @Override
    public JsonToken nextToken() throws IOException {
        _cursorEntry = jsonTokenIterator.next();
        _currToken = _cursorEntry.jsonToken;
        return _currToken;
    }

    @Override
    protected void _handleEOF() throws JsonParseException {
        logger.debug(Thread.currentThread() + " _handleEOF() called with: ");
    }


    private int flatJavaType(LinkedList<FieldCursor> map, JavaType javaType, int cursor, int maxLength) {
        AnnotatedClass classInfo = getDeserializationConfig().introspect(javaType).getClassInfo();
        Quantity quantity = classInfo.getAnnotation(Quantity.class);
        int length = 0;
        JsonToken type = getJavaTypeValueType(javaType);
        map.add(new FieldCursor(type));
        switch (type) {
            case START_OBJECT:
                Iterable<AnnotatedField> fields = classInfo.fields();
                for (AnnotatedField field : fields) {
                    int quantityLength = getQuantityLength(field);
                    if (quantityLength != 0) {
                        FieldCursor fieldCursor = new FieldCursor(JsonToken.FIELD_NAME, field,
                                cursor + length,
                                cursor + length + quantityLength);
                        map.add(fieldCursor);
                        int i = flatJavaType(map, field.getType(), cursor + length, quantityLength);
                        if (i > quantityLength) {
                            throw new UnsupportedOperationException("Read array out of bounds");
                        }
                        length += quantityLength;
                    }
                }
                map.add(new FieldCursor(JsonToken.END_OBJECT));
                break;
            case START_ARRAY:
                int arraySize = determineArraySize(javaType, maxLength);
                for (int i = arraySize; i > 0; i--) {
                    int i1 = flatJavaType(map, javaType.getContentType(), cursor + length, maxLength);
                    length += i1;
                }
                map.add(new FieldCursor(JsonToken.END_ARRAY));
                break;
        }
        if (quantity != null && quantity.value() != length) {
            throw new UnsupportedOperationException("Read array out of bounds");
        }
        return length;
    }

    private JsonToken getJavaTypeValueType(JavaType javaType) {
        if (javaType.isArrayType() || javaType.isCollectionLikeType()) {
            return JsonToken.START_ARRAY;
        } else if (javaType.isTypeOrSubTypeOf(CharSequence.class)) {
            return JsonToken.VALUE_STRING;
        } else if (javaType.isTypeOrSubTypeOf(Byte.class)
                || javaType.isTypeOrSubTypeOf(byte.class)
                || javaType.isTypeOrSubTypeOf(Short.class)
                || javaType.isTypeOrSubTypeOf(short.class)
                || javaType.isTypeOrSubTypeOf(Integer.class)
                || javaType.isTypeOrSubTypeOf(int.class)
                || javaType.isTypeOrSubTypeOf(Long.class)
                || javaType.isTypeOrSubTypeOf(long.class)) {
            return JsonToken.VALUE_NUMBER_INT;
        } else if (javaType.isTypeOrSubTypeOf(Number.class)
                || javaType.isTypeOrSubTypeOf(double.class)
                || javaType.isTypeOrSubTypeOf(float.class)) {
            return JsonToken.VALUE_NUMBER_FLOAT;
        } else if (javaType.isTypeOrSubTypeOf(Boolean.class)
                || javaType.isTypeOrSubTypeOf(boolean.class)) {
            // 无法在反射的时候确定 JsonToken.VALUE_FALSE或者 JsonToken.VALUE_TRUE使用JsonToken.VALUE_NUMBER_INT代替。
            return JsonToken.VALUE_NUMBER_INT;
        } else if (javaType.isTypeOrSubTypeOf(Object.class)) {
            return JsonToken.START_OBJECT;
        } else {
            throw new UnsupportedOperationException(javaType.toString());
        }
    }

    private int determineArraySize(JavaType javaType, int length) {
        int size = 0;
        JavaType contentType = javaType.getContentType();
        AnnotatedClass classInfo = getDeserializationConfig().introspect(contentType).getClassInfo();
        Quantity quantity = classInfo.getAnnotation(Quantity.class);
        if (quantity != null) {
            return length / quantity.value();
        } else if (contentType.isArrayType() || contentType.isContainerType()) {
            throw new UnsupportedOperationException(
                    "The expected data length does not match the actual length");
        } else {
            int objectLength = determineObjectLength(contentType);
            if (length % objectLength > 0) {
                throw new UnsupportedOperationException(
                        "The expected data length does not match the actual length " + length + " " + objectLength);
            }
            return length / objectLength;
        }
    }

    private int determineObjectLength(JavaType javaType) {
        AnnotatedClass classInfo = getDeserializationConfig().introspect(
                javaType).getClassInfo();
        Quantity annotation = classInfo.getAnnotation(Quantity.class);
        if (annotation != null) {
            return annotation.value();
        } else {
            int total = 0;
            Iterable<AnnotatedField> fields = classInfo.fields();
            for (AnnotatedField field : fields) {
                total += getQuantityLength(field);
            }
            return total;
        }
    }

    private int getQuantityLength(Annotated annotated) {
        Quantity quantity = annotated.getAnnotation(Quantity.class);
        if (quantity != null) {
            return quantity.value() * 2;
        }
        return 0;
    }

    public DeserializationConfig getDeserializationConfig() {
        return ((ModbusMapper) codec).getDeserializationConfig();
    }

    @Override
    public String getCurrentName() throws IOException {
        _currentFieldCursor = _cursorEntry;
        return _cursorEntry.field.getName();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public ObjectCodec getCodec() {
        return null;
    }

    @Override
    public void setCodec(ObjectCodec c) {
    }

    @Override
    public Version version() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return jsonTokenIterator == null || !jsonTokenIterator.hasNext();
    }

    @Override
    public JsonStreamContext getParsingContext() {
        return null;
    }

    @Override
    public void overrideCurrentName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public JsonLocation getTokenLocation() {
        return null;
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return null;
    }

    @Override
    public String getText() throws IOException {
        FieldCursor cursor = _currentFieldCursor;
        String text = new String(inputBuffer, cursor.inputStr, cursor.inputEnd - cursor.inputStr, cursor.getCharset());
        return text;
    }

    @Override
    public char[] getTextCharacters() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTextCharacters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTextLength() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTextOffset() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Number getNumberValue() throws IOException {
        return getLongValue();
    }

    @Override
    public NumberType getNumberType() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getIntValue() throws IOException {
        return Math.toIntExact(getLongValue());
    }

    @Override
    public long getLongValue() throws IOException {
        ByteBuf byteBuffer = getCurrentByteBuf();
        int readableBytes = byteBuffer.readableBytes();
        switch (readableBytes) {
            case 8:
                return byteBuffer.readLong();
            case 4:
                return byteBuffer.readInt();
            case 2:
                return byteBuffer.readShort();
            case 1:
                return byteBuffer.readByte();
            default:
                throw new UnsupportedOperationException("byte length " + readableBytes + "cannot be converted to a number type");
        }
    }

    @Override
    public BigInteger getBigIntegerValue() throws IOException {
        return BigInteger.valueOf(getLongValue());
    }

    @Override
    public float getFloatValue() throws IOException {
        ByteBuf byteBuffer = getCurrentByteBuf();
        int readableBytes = byteBuffer.readableBytes();
        if (readableBytes != 4) {
            throw new UnsupportedOperationException("byte length " + readableBytes + "cannot be converted to a number type");
        }
        return byteBuffer.readFloat();
    }

    @Override
    public double getDoubleValue() throws IOException {
        ByteBuf byteBuffer = getCurrentByteBuf();
        int readableBytes = byteBuffer.readableBytes();
        if (readableBytes != 8) {
            throw new UnsupportedOperationException("byte length " + readableBytes + "cannot be converted to a number type");
        }
        return byteBuffer.readDouble();
    }

    private ByteBuf getCurrentByteBuf() {
        FieldCursor cursor = _currentFieldCursor;
        int i1 = cursor.inputEnd - cursor.inputStr;
        return Unpooled.wrappedBuffer(inputBuffer, cursor.inputStr, i1);
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException {
        ByteBuf byteBuffer = getCurrentByteBuf();
        int readableBytes = byteBuffer.readableBytes();
        switch (readableBytes) {
            case 8:
                return BigDecimal.valueOf(byteBuffer.readDouble());
            case 4:
                return BigDecimal.valueOf(byteBuffer.readDouble());
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}

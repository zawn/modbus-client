package org.modbus.jackson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.base.GeneratorBase;
import com.fasterxml.jackson.core.io.IOContext;

/**
 * @author zhangzhenli
 */
public class ModbusGenerator extends GeneratorBase {
    private static final Logger logger = LoggerFactory.getLogger(ModbusGenerator.class);


    private final IOContext ctxt;
    private final OutputStream out;

    public ModbusGenerator(IOContext ctxt, int features,
                           ObjectCodec codec, OutputStream out) {
        super(features, codec);
        this.ctxt = ctxt;
        this.out = out;
    }

    @Override
    public void writeStartArray() throws IOException {
        logger.debug("writeStartArray() called");
    }

    @Override
    public void writeEndArray() throws IOException {
        logger.debug("writeEndArray() called");
    }

    @Override
    public void writeStartObject() throws IOException {
        logger.debug("writeStartObject() called");
    }

    @Override
    public void writeEndObject() throws IOException {
        logger.debug("writeEndObject() called");
    }

    @Override
    public void writeFieldName(String name) throws IOException {
        logger.debug("writeFieldName() called with: name = [" + name + "]");
    }

    @Override
    public void writeString(String text) throws IOException {
        logger.debug("writeString() called with: text = [" + text + "]");
    }

    @Override
    public void writeString(char[] text, int offset, int len) throws IOException {
        logger.debug(
                "writeString() called with: text = [" + text + "], offset = [" + offset + "], len = [" + len + "]");
    }

    @Override
    public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {
        logger.debug(
                "writeRawUTF8String() called with: text = [" + text + "], offset = [" + offset + "], length = [" + length + "]");
    }

    @Override
    public void writeUTF8String(byte[] text, int offset, int length) throws IOException {
        logger.debug(
                "writeUTF8String() called with: text = [" + text + "], offset = [" + offset + "], length = [" + length + "]");
    }

    @Override
    public void writeRaw(String text) throws IOException {
        logger.debug("writeRaw() called with: text = [" + text + "]");
    }

    @Override
    public void writeRaw(String text, int offset, int len) throws IOException {
        logger.debug(
                "writeRaw() called with: text = [" + text + "], offset = [" + offset + "], len = [" + len + "]");
    }

    @Override
    public void writeRaw(char[] text, int offset, int len) throws IOException {
        logger.debug(
                "writeRaw() called with: text = [" + text + "], offset = [" + offset + "], len = [" + len + "]");
    }

    @Override
    public void writeRaw(char c) throws IOException {
        logger.debug("writeRaw() called with: c = [" + c + "]");
    }

    @Override
    public void writeBinary(Base64Variant bv, byte[] data, int offset, int len) throws IOException {
        logger.debug(
                "writeBinary() called with: bv = [" + bv + "], data = [" + data + "], offset = [" + offset + "], len = [" + len + "]");
        out.write(data, offset, len);
    }

    @Override
    public void writeNumber(int v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(long v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(BigInteger v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(double v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(float v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(BigDecimal v) throws IOException {
        logger.debug("writeNumber() called with: v = [" + v + "]");
    }

    @Override
    public void writeNumber(String encodedValue) throws IOException {
        logger.debug("writeNumber() called with: encodedValue = [" + encodedValue + "]");
    }

    @Override
    public void writeBoolean(boolean state) throws IOException {
        logger.debug("writeBoolean() called with: state = [" + state + "]");
    }

    @Override
    public void writeNull() throws IOException {
        logger.debug("writeNull() called");
    }

    @Override
    public void flush() throws IOException {
        logger.debug("flush() called");
    }

    @Override
    protected void _releaseBuffers() {
        logger.debug("_releaseBuffers() called");
    }

    @Override
    protected void _verifyValueWrite(String typeMsg) throws IOException {
        logger.debug("_verifyValueWrite() called with: typeMsg = [" + typeMsg + "]");
    }
}

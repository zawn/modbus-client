package org.modbus.jackson;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.JavaType;

import java.io.*;

/**
 * @author zhangzhenli
 */
public class ModbusFactory extends JsonFactory {


    public ModbusFactory(ObjectCodec oc) {
        super(oc);
    }

    public ModbusParser createParser(InputStream in,
                                     JavaType javaType) throws IOException, JsonParseException {
        IOContext ctxt = _createContext(in, true);
        return _createParser(in, ctxt, javaType);
    }

    public ModbusParser createParser(byte[] data,
                                     JavaType javaType) throws IOException, JsonParseException {
        IOContext ctxt = _createContext(data, true);
        return _createParser(data, 0, data.length, _createContext(data, true), javaType);
    }

    protected ModbusParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        return _createParser(in, ctxt, null);
    }

    protected ModbusParser _createParser(InputStream in, IOContext ctxt,
                                         JavaType javaType) throws IOException {
        return new ModbusParserBootstrapper(ctxt, in).constructParser(_factoryFeatures,
                _parserFeatures,
                _objectCodec, _byteSymbolCanonicalizer, javaType);
    }

    @Override
    protected JsonParser _createParser(Reader r, IOContext ctxt) throws IOException {
        return _nonByteSource();
    }

    @Override
    protected JsonParser _createParser(char[] data, int offset, int len, IOContext ctxt,
                                       boolean recyclable) throws IOException {
        return _nonByteSource();
    }

    @Override
    protected JsonParser _createParser(byte[] data, int offset, int len,
                                       IOContext ctxt) throws IOException {
        return _createParser(data, offset, len, ctxt, null);
    }

    protected ModbusParser _createParser(byte[] data, int offset, int len,
                                         IOContext ctxt, JavaType javaType) throws IOException {
        return new ModbusParserBootstrapper(ctxt, data, offset, len).constructParser(
                _factoryFeatures, _parserFeatures,
                _objectCodec, _byteSymbolCanonicalizer, javaType);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        return createGenerator(out);
    }

    @Override
    public JsonGenerator createGenerator(Writer w) throws IOException {
        return _nonByteTarget();
    }

    @Override
    public JsonGenerator createGenerator(File f, JsonEncoding enc) throws IOException {
        return createGenerator(new FileOutputStream(f));
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        // false -> we won't manage the stream unless explicitly directed to
        IOContext ctxt = _createContext(out, false);
        return _createGenerator(_decorate(out, ctxt), ctxt);
    }

    protected ModbusGenerator _createGenerator(OutputStream out,
                                               IOContext ctxt) throws IOException {
        ModbusGenerator gen = new ModbusGenerator(ctxt, _generatorFeatures,
                _objectCodec, out);
        return gen;
    }

    protected <T> T _nonByteSource() throws IOException {
        throw new UnsupportedOperationException(
                "Can not create parser for character-based (not byte-based) source");
    }

    protected <T> T _nonByteTarget() throws IOException {
        throw new UnsupportedOperationException(
                "Can not create generator for character-based (not byte-based) target");
    }
}

package org.modbus.jackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.databind.JavaType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple bootstrapper version used with CBOR format parser.
 */
public class ModbusParserBootstrapper {
    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    protected final IOContext _context;
    protected final InputStream _in;

    /*
    /**********************************************************
    /* Input buffering
    /**********************************************************
     */

    protected final byte[] _inputBuffer;
    protected int _inputStr, _inputEnd;

    /**
     * Flag that indicates whether buffer above is to be recycled
     * after being used or not.
     */
    protected final boolean _bufferRecyclable;

    /*
    /**********************************************************
    /* Input location
    /**********************************************************
     */

    /**
     * Current number of input units (bytes or chars) that were processed in
     * previous blocks,
     * before contents of current input buffer.
     * <p>
     * Note: includes possible BOMs, if those were part of the input.
     */
    protected int _inputProcessed;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public ModbusParserBootstrapper(IOContext ctxt, InputStream in) {
        _context = ctxt;
        _in = in;
        _inputBuffer = ctxt.allocReadIOBuffer();
        _inputEnd = _inputStr = 0;
        _inputProcessed = 0;
        _bufferRecyclable = true;
    }

    public ModbusParserBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart,
                                    int inputLen) {
        _context = ctxt;
        _in = null;
        _inputBuffer = inputBuffer;
        _inputStr = inputStart;
        _inputEnd = (inputStart + inputLen);
        // Need to offset this for correct location info
        _inputProcessed = -inputStart;
        _bufferRecyclable = false;
    }

    public ModbusParser constructParser(int factoryFeatures,
                                        int generalParserFeatures,
                                        ObjectCodec codec, ByteQuadsCanonicalizer rootByteSymbols,
                                        JavaType javaType)
            throws IOException, JsonParseException {
        ByteQuadsCanonicalizer can = rootByteSymbols.makeChild(factoryFeatures);
        // We just need a single byte to recognize possible "empty" document.
        ensureLoaded(1);
        ModbusParser p = new ModbusParser(
                codec,
                _in, _inputBuffer, _inputStr, _inputEnd, _bufferRecyclable, javaType);
        if (_inputStr < _inputEnd) { // only false for empty doc
            ; // anything we should verify? In future, could verify
        } else {
            /* 13-Jan-2014, tatu: Actually, let's allow empty documents even if
             *   header signature would otherwise be needed. This is useful for
             *   JAX-RS provider, empty PUT/POST payloads?
             */
            ;
        }
        return p;
    }

    /*
    /**********************************************************
    /*  Encoding detection for data format auto-detection
    /**********************************************************
     */

    /*
    /**********************************************************
    /* Internal methods, raw input access
    /**********************************************************
     */

    protected boolean ensureLoaded(int minimum) throws IOException {
        if (_in == null) { // block source; nothing more to load
            return false;
        }

        /* Let's assume here buffer has enough room -- this will always
         * be true for the limited used this method gets
         */
        int gotten = (_inputEnd - _inputStr);
        while (gotten < minimum) {
            int count = _in.read(_inputBuffer, _inputEnd, _inputBuffer.length - _inputEnd);
            if (count < 1) {
                return false;
            }
            _inputEnd += count;
            gotten += count;
        }
        return true;
    }
}

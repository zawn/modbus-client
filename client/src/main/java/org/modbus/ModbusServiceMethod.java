package org.modbus;

import org.modbus.io.ModbusClient;

import io.netty.buffer.ByteBuf;

/**
 * @author zhangzhenli
 */
public class ModbusServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

    private ModbusRequestFactory requestFactory;
    private ModbusClient callFactory;
    private final CallAdapter<ResponseT, ReturnT> callAdapter;
    private final Converter<ByteBuf, ResponseT> responseConverter;

    ModbusServiceMethod(ModbusRequestFactory requestFactory,
                        ModbusClient callFactory,
                        CallAdapter<ResponseT, ReturnT> callAdapter,
                        Converter<ByteBuf, ResponseT> responseConverter) {
        this.requestFactory = requestFactory;
        this.callFactory = callFactory;
        this.callAdapter = callAdapter;
        this.responseConverter = responseConverter;
    }

    @Override
    public ReturnT invoke(Object[] args) {
        return callAdapter.adapt(
                new ModbusCall<>(requestFactory,args, callFactory, responseConverter));
    }
}

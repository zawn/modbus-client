package org.modbus;

/**
 * @author zhangzhenli
 */
public class ModbusServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

    private ModbusRequestFactory requestFactory;
    private final CallAdapter<ResponseT, ReturnT> callAdapter;
    private final Converter<Response, ResponseT> responseConverter;

    ModbusServiceMethod(ModbusRequestFactory requestFactory,
                        CallAdapter<ResponseT, ReturnT> callAdapter,
                        Converter<Response, ResponseT> responseConverter) {
        this.requestFactory = requestFactory;
        this.callAdapter = callAdapter;
        this.responseConverter = responseConverter;
    }

    @Override
    public ReturnT invoke(Object[] args) {
        return callAdapter.adapt(
                new ModbusCall<>(requestFactory,args, responseConverter));
    }
}

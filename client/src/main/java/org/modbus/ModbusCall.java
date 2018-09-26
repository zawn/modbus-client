package org.modbus;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author zhangzhenli
 */
public class ModbusCall<T> implements Call<T> {

    private ModbusRequestFactory requestFactory;
    private final Object[] args;
    private final Converter<Response, T> responseConverter;
    Logger logger = Logger.getLogger(ModbusCall.class.getName());

    public ModbusCall(ModbusRequestFactory requestFactory,
                      Object[] args, Converter<Response, T> responseConverter) {
        this.requestFactory = requestFactory;
        this.args = args;
        this.responseConverter = responseConverter;
    }

    @Override
    public Response<T> execute() throws IOException {
        System.out.println("ModbusCall.execute");
        return null;
    }

    @Override
    public void enqueue(Callback<T> callback) {
        System.out.println("ModbusCall.enqueue");
    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public void cancel() {
        System.out.println("ModbusCall.cancel");
    }

    @Override
    public boolean isCanceled() {
        System.out.println("ModbusCall.isCanceled");
        return false;
    }

    @Override
    public Call<T> clone() {
        System.out.println("ModbusCall.clone");
        return null;
    }

    @Override
    public Request request() {
        System.out.println("ModbusCall.request");
        return null;
    }
}


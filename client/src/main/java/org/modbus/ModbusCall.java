package org.modbus;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.modbus.io.ModbusClient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author zhangzhenli
 */
public class ModbusCall<T> implements Call<T> {

    private ModbusRequestFactory requestFactory;
    private final Object[] args;
    private ModbusClient callFactory;
    private final Converter<ByteBuf, T> responseConverter;
    Logger logger = Logger.getLogger(ModbusCall.class.getName());

    public ModbusCall(ModbusRequestFactory requestFactory,
                      Object[] args,
                      ModbusClient callFactory,
                      Converter<ByteBuf, T> responseConverter) {
        this.requestFactory = requestFactory;
        this.args = args;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    @Override
    public Response<T> execute() throws IOException {
        System.out.println("ModbusCall.execute");
        try {
            switch (requestFactory.modbusMethod) {
                case "WRITE":
                    callFactory.write(requestFactory.start, requestFactory.quantity, requestFactory.create(args), 1);
                    Class<Void> v= Void.TYPE;
                    return new Response<>(null,null);
                case "READ":
                    byte[] bytes = callFactory.read(requestFactory.start, requestFactory.quantity, 1);
                    ByteBuf byteBuffer = Unpooled.wrappedBuffer(bytes);
                    T convert = responseConverter.convert(byteBuffer);
                    return  new Response<>(byteBuffer,convert);
                default:
                    break;
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
    public byte[] request() {
        System.out.println("ModbusCall.request");
        return null;
    }
}


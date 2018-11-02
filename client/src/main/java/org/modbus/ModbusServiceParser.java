package org.modbus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.modbus.io.ModbusClient;

import io.netty.buffer.ByteBuf;

import static org.modbus.Utils.methodError;

/**
 * @author zhangzhenli
 */
public class ModbusServiceParser extends ServiceParser {


    private ModbusClient callFactory;

    public ModbusServiceParser(ModbusClient callFactory) {
        this.callFactory = callFactory;
    }

    @Override
    public <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method) {
        ModbusRequestFactory requestFactory = ModbusRequestFactory.parseAnnotations(retrofit,
                method);

        Type returnType = method.getGenericReturnType();
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(method,
                    "Method return type must not include a type variable or wildcard: %s",
                    returnType);
        }
        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }

        CallAdapter<Object, T> callAdapter = createCallAdapter(retrofit, method);
        Type responseType = callAdapter.responseType();
        if (responseType == Response.class) {
            throw methodError(method, "'"
                    + Utils.getRawType(responseType).getName()
                    + "' is not a valid response body type. Did you mean ResponseBody?");
        }
        if (requestFactory.modbusMethod.equals("HEAD") && !Void.class.equals(responseType)) {
            throw methodError(method, "HEAD method must use Void as response type.");
        }



        Converter<ByteBuf, Object> responseConverter =
                createResponseConverter(retrofit, method, responseType);

        return new ModbusServiceMethod<>(requestFactory,callFactory,callAdapter, responseConverter);
    }

    private static <ResponseT, ReturnT> CallAdapter<ResponseT, ReturnT> createCallAdapter(
            Retrofit retrofit, Method method) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        try {
            //noinspection unchecked
            return (CallAdapter<ResponseT, ReturnT>) retrofit.callAdapter(returnType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create call adapter for %s", returnType);
        }
    }

    private static <ResponseT> Converter<ByteBuf, ResponseT> createResponseConverter(
            Retrofit retrofit, Method method, Type responseType) {
        Annotation[] annotations = method.getAnnotations();
        try {
            return retrofit.responseBodyConverter(responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create converter for %s", responseType);
        }
    }

    public ModbusClient getCallFactory() {
        return callFactory;
    }
}
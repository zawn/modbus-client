package org.modbus;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.modbus.ParameterHandler.ParameterHandlerImpl;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.modbus.Utils.methodError;
import static org.modbus.Utils.parameterError;

/**
 * @author zhangzhenli
 */
public class ModbusRequestFactory {
    public static ModbusRequestFactory parseAnnotations(Retrofit retrofit, Method method) {
        return new Builder(retrofit, method).build();
    }

    private final Method method;
    private final String baseUrl;
    final String modbusMethod;
    private final boolean hasBody;
    ParameterHandler<?>[] parameterHandlers;

    int start;
    int quantity;

    ModbusRequestFactory(Builder builder) {
        method = builder.method;
        baseUrl = builder.retrofit.baseUrl();
        modbusMethod = builder.modbusMethod;
        hasBody = builder.hasBody;
        start = builder.start;
        quantity = builder.quantity;
        parameterHandlers = builder.parameterHandlers;
    }

    ByteBuf create(Object[] args) throws IOException {
        @SuppressWarnings("unchecked") // It is an error to invoke a method with the wrong arg types.
                ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;

        int argumentCount = args.length;
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }

        ByteBuf requestBuilder = Unpooled.buffer();

        List<Object> argumentList = new ArrayList<>(argumentCount);
        for (int p = 0; p < argumentCount; p++) {
            argumentList.add(args[p]);
            handlers[p].apply(requestBuilder, args[p]);
        }

        return requestBuilder;
    }

    /**
     * Inspects the annotations on an interface method to construct a reusable service method. This
     * requires potentially-expensive reflection so it is best to build each service method only once
     * and reuse it. Builders cannot be reused.
     */
    static final class Builder {
        // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
        private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";

        final Retrofit retrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final Annotation[][] parameterAnnotationsArray;
        final Type[] parameterTypes;

        String modbusMethod;
        boolean hasBody;
        boolean isFormEncoded;
        boolean isMultipart;
        int start;
        int quantity;
        ParameterHandler<?>[] parameterHandlers;

        Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();

            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];
            for (int p = 0; p < parameterCount; p++) {
                parameterHandlers[p] = parseParameter(p, parameterTypes[p], parameterAnnotationsArray[p]);
            }
        }

        ModbusRequestFactory build() {
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            if (modbusMethod == null) {
                throw methodError(method,
                        "HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            }

            if (!hasBody) {
                if (isMultipart) {
                    throw methodError(method,
                            "Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                }
                if (isFormEncoded) {
                    throw methodError(method,
                            "FormUrlEncoded can only be specified on HTTP methods with "
                                    + "request body (e.g., @POST).");
                }
            }
            return new ModbusRequestFactory(this);
        }

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof WRITE) {
                int start = ((WRITE) annotation).start();
                int quantity = ((WRITE) annotation).quantity();
                int end = ((WRITE) annotation).end();
                if (quantity == 0 && end != 0) {
                    quantity = end - start + 1;
                }
                parseHttpMethodAndPath("WRITE", start, quantity, true);
            } else if (annotation instanceof READ) {
                int start = ((READ) annotation).start();
                int quantity = ((READ) annotation).quantity();
                int end = ((READ) annotation).end();
                if (quantity == 0 && end != 0) {
                    quantity = end - start + 1;
                }
                parseHttpMethodAndPath("READ", start, quantity, false);
            }
        }

        private void parseHttpMethodAndPath(String httpMethod, int start, int quantity,
                                            boolean hasBody) {
            if (this.modbusMethod != null) {
                throw methodError(method, "Only one HTTP method is allowed. Found: %s and %s.",
                        this.modbusMethod, httpMethod);
            }
            this.modbusMethod = httpMethod;
            this.hasBody = hasBody;

            if (quantity == 0) {
                return;
            }
            this.start = start;
            this.quantity = quantity;
        }


        private ParameterHandler<?> parseParameter(
                int p, Type parameterType, @Nullable Annotation[] annotations) {
            ParameterHandler<?> result = null;
            if (annotations != null) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(p, parameterType, annotations);

                if (result != null) {
                    throw parameterError(method, p,
                            "Multiple Retrofit annotations found, only one allowed.");
                }

                result = annotationAction;
            }

            if (result == null) {
                throw parameterError(method, p, "No Retrofit annotation found.");
            }

            return result;
        }

        private ParameterHandler<?> parseParameterAnnotation(
                int p, Type type, Annotation[] annotations) {
            validateResolvableType(p, type);
            Class<?> rawParameterType = Utils.getRawType(type);
            if (Iterable.class.isAssignableFrom(rawParameterType)) {
                if (!(type instanceof ParameterizedType)) {
                    throw parameterError(method, p, rawParameterType.getSimpleName()
                            + " must include generic type (e.g., "
                            + rawParameterType.getSimpleName()
                            + "<String>)");
                }
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type iterableType = Utils.getParameterUpperBound(0, parameterizedType);
                Converter<?, ByteBuf> converter =
                        retrofit.requestBodyConverter(iterableType, annotations, methodAnnotations);
                return new ParameterHandlerImpl<>(converter).iterable();
            } else if (rawParameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(rawParameterType.getComponentType());
                Converter<?, ByteBuf> converter =
                        retrofit.requestBodyConverter(arrayComponentType, annotations, methodAnnotations);
                return new ParameterHandlerImpl<>(converter).array();
            } else {
                Converter<?, ByteBuf> converter =
                        retrofit.requestBodyConverter(rawParameterType, annotations, methodAnnotations);
                return new ParameterHandlerImpl<>(converter);
            }
        }

        private void validateResolvableType(int p, Type type) {
            if (Utils.hasUnresolvableType(type)) {
                throw parameterError(method, p,
                        "Parameter type must not include a type variable or wildcard: %s", type);
            }
        }

    }

    private static Class<?> boxIfPrimitive(Class<?> type) {
        if (boolean.class == type) return Boolean.class;
        if (byte.class == type) return Byte.class;
        if (char.class == type) return Character.class;
        if (double.class == type) return Double.class;
        if (float.class == type) return Float.class;
        if (int.class == type) return Integer.class;
        if (long.class == type) return Long.class;
        if (short.class == type) return Short.class;
        return type;
    }
}

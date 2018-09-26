package org.modbus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;

import static org.modbus.Utils.methodError;

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

    int start;
    int quantity;

    ModbusRequestFactory(Builder builder) {
        method = builder.method;
        baseUrl = builder.retrofit.baseUrl();
        modbusMethod = builder.modbusMethod;
        hasBody = builder.hasBody;
        start = builder.start;
        quantity = builder.quantity;
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

        Builder(Retrofit retrofit, Method method) {
            this.retrofit = retrofit;
            this.method = method;
            this.methodAnnotations = method.getAnnotations();
            this.parameterTypes = method.getGenericParameterTypes();
            this.parameterAnnotationsArray = method.getParameterAnnotations();
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
    }
}

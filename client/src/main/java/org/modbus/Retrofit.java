/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modbus;

import io.netty.buffer.ByteBuf;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import static java.util.Collections.unmodifiableList;
import static org.modbus.Utils.checkNotNull;

/**
 * Retrofit adapts a Java interface to HTTP calls by using annotations on the declared methods to
 * define how requests are made. Create instances using {@linkplain Builder
 * the builder} and pass your interface to {@link #create} to generate an implementation.
 * <p>
 * For example,
 * <pre><code>
 * Retrofit retrofit = new Retrofit.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .addConverterFactory(ModbusConverterFactory.create())
 *     .build();
 *
 * MyApi api = retrofit.create(MyApi.class);
 * Response&lt;User&gt; user = api.getUser().execute();
 * </code></pre>
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public final class Retrofit {
  private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();

  final String baseUrl;
  final List<Converter.Factory> converterFactories;
  final List<CallAdapter.Factory> callAdapterFactories;
  final @Nullable Executor callbackExecutor;
  final boolean validateEagerly;
  final ServiceParser serviceParser;

  Retrofit(ServiceParser serviceParser, String baseUrl,
           List<Converter.Factory> converterFactories, List<CallAdapter.Factory> callAdapterFactories,
           @Nullable Executor callbackExecutor, boolean validateEagerly) {
    this.serviceParser = serviceParser;
    this.baseUrl = baseUrl;
    this.converterFactories = converterFactories; // Copy+unmodifiable at call site.
    this.callAdapterFactories = callAdapterFactories; // Copy+unmodifiable at call site.
    this.callbackExecutor = callbackExecutor;
    this.validateEagerly = validateEagerly;
  }

  /**
   * Create an implementation of the API endpoints defined by the {@code service} interface.
   * <p>
   * The relative path for a given method is obtained from an annotation on the method describing
   * the request type. The built-in methods are {@link retrofit2.http.GET GET},
   * {@link retrofit2.http.PUT PUT}, {@link retrofit2.http.POST POST}, {@link retrofit2.http.PATCH
   * PATCH}, {@link retrofit2.http.HEAD HEAD}, {@link retrofit2.http.DELETE DELETE} and
   * {@link retrofit2.http.OPTIONS OPTIONS}. You can use a custom HTTP method with
   * {@link HTTP @HTTP}. For a dynamic URL, omit the path on the annotation and annotate the first
   * parameter with {@link Url @Url}.
   * <p>
   * Method parameters can be used to replace parts of the URL by annotating them with
   * {@link retrofit2.http.Path @Path}. Replacement sections are denoted by an identifier
   * surrounded by curly braces (e.g., "{foo}"). To add items to the query string of a URL use
   * {@link retrofit2.http.Query @Query}.
   * <p>
   * The body of a request is denoted by the {@link retrofit2.http.Body @Body} annotation. The
   * object will be converted to request representation by one of the {@link Converter.Factory}
   * instances. A {@link RequestBody} can also be used for a raw representation.
   * <p>
   * Alternative request body formats are supported by method annotations and corresponding
   * parameter annotations:
   * <ul>
   * <li>{@link retrofit2.http.FormUrlEncoded @FormUrlEncoded} - Form-encoded data with key-value
   * pairs specified by the {@link retrofit2.http.Field @Field} parameter annotation.
   * <li>{@link retrofit2.http.Multipart @Multipart} - RFC 2388-compliant multipart data with
   * parts specified by the {@link retrofit2.http.Part @Part} parameter annotation.
   * </ul>
   * <p>
   * Additional static headers can be added for an endpoint using the
   * {@link retrofit2.http.Headers @Headers} method annotation. For per-request control over a
   * header annotate a parameter with {@link Header @Header}.
   * <p>
   * By default, methods return a {@link Call} which represents the HTTP request. The generic
   * parameter of the call is the response body type and will be converted by one of the
   * {@link Converter.Factory} instances. {@link ResponseBody} can also be used for a raw
   * representation. {@link Void} can be used if you do not care about the body contents.
   * <p>
   * For example:
   * <pre>
   * public interface CategoryService {
   *   &#64;POST("category/{cat}/")
   *   Call&lt;List&lt;Item&gt;&gt; categoryList(@Path("cat") String a, @Query("page") int b);
   * }
   * </pre>
   */
  @SuppressWarnings("unchecked") // Single-interface proxy creation guarded by parameter safety.
  public <T> T create(final Class<T> service) {
    Utils.validateServiceInterface(service);
    if (validateEagerly) {
      eagerlyValidateMethods(service);
    }
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          private final Platform platform = Platform.get();
          private final Object[] emptyArgs = new Object[0];

          @Override public Object invoke(Object proxy, Method method, @Nullable Object[] args)
              throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            if (platform.isDefaultMethod(method)) {
              return platform.invokeDefaultMethod(method, service, proxy, args);
            }
            return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
          }
        });
  }

  private void eagerlyValidateMethods(Class<?> service) {
    Platform platform = Platform.get();
    for (Method method : service.getDeclaredMethods()) {
      if (!platform.isDefaultMethod(method)) {
        loadServiceMethod(method);
      }
    }
  }

  ServiceMethod<?> loadServiceMethod(Method method) {
    ServiceMethod<?> result = serviceMethodCache.get(method);
    if (result != null) return result;

    synchronized (serviceMethodCache) {
      result = serviceMethodCache.get(method);
      if (result == null) {
        result = serviceParser.parseAnnotations(this, method);
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }

  /** The API base URL. */
  public String baseUrl() {
    return baseUrl;
  }

  /**
   * Returns a list of the factories tried when creating a
   * {@linkplain #callAdapter(Type, Annotation[])} call adapter}.
   */
  public List<CallAdapter.Factory> callAdapterFactories() {
    return callAdapterFactories;
  }

  /**
   * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
   * #callAdapterFactories() factories}.
   *
   * @throws IllegalArgumentException if no call adapter available for {@code type}.
   */
  public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
    return nextCallAdapter(null, returnType, annotations);
  }

  /**
   * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
   * #callAdapterFactories() factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no call adapter available for {@code type}.
   */
  public CallAdapter<?, ?> nextCallAdapter(@Nullable CallAdapter.Factory skipPast, Type returnType,
      Annotation[] annotations) {
    checkNotNull(returnType, "returnType == null");
    checkNotNull(annotations, "annotations == null");

    int start = callAdapterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
      CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
      if (adapter != null) {
        return adapter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
        .append(returnType)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * Returns an unmodifiable list of the factories tried when creating a
   * {@linkplain #requestBodyConverter(Type, Annotation[], Annotation[]) request body converter}, a
   * {@linkplain #responseBodyConverter(Type, Annotation[]) response body converter}, or a
   * {@linkplain #stringConverter(Type, Annotation[]) string converter}.
   */
  public List<Converter.Factory> converterFactories() {
    return converterFactories;
  }

  /**
   * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
   * {@linkplain #converterFactories() factories}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<T, ByteBuf> requestBodyConverter(Type type,
                                                        Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
    return nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
  }

  /**
   * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
   * {@linkplain #converterFactories() factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<T, ByteBuf> nextRequestBodyConverter(
      @Nullable Converter.Factory skipPast, Type type, Annotation[] parameterAnnotations,
      Annotation[] methodAnnotations) {
    checkNotNull(type, "type == null");
    checkNotNull(parameterAnnotations, "parameterAnnotations == null");
    checkNotNull(methodAnnotations, "methodAnnotations == null");

    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      Converter.Factory factory = converterFactories.get(i);
      Converter<?, ByteBuf> converter =
          factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<T, ByteBuf>) converter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
        .append(type)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
   * {@linkplain #converterFactories() factories}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<ByteBuf, T> responseBodyConverter(Type type, Annotation[] annotations) {
    return nextResponseBodyConverter(null, type, annotations);
  }

  /**
   * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
   * {@linkplain #converterFactories() factories} except {@code skipPast}.
   *
   * @throws IllegalArgumentException if no converter available for {@code type}.
   */
  public <T> Converter<ByteBuf, T> nextResponseBodyConverter(
      @Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
    checkNotNull(type, "type == null");
    checkNotNull(annotations, "annotations == null");

    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      Converter<ByteBuf, ?> converter =
          converterFactories.get(i).responseBodyConverter(type, annotations, this);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<ByteBuf, T>) converter;
      }
    }

    StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
        .append(type)
        .append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int i = 0; i < start; i++) {
        builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
      }
      builder.append('\n');
    }
    builder.append("  Tried:");
    for (int i = start, count = converterFactories.size(); i < count; i++) {
      builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
    }
    throw new IllegalArgumentException(builder.toString());
  }

  /**
   * The executor used for {@link Callback} methods on a {@link Call}. This may be {@code null},
   * in which case callbacks should be made synchronously on the background thread.
   */
  public @Nullable Executor callbackExecutor() {
    return callbackExecutor;
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  /**
   * Build a new {@link Retrofit}.
   * <p>
   * Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods
   * are optional.
   */
  public static final class Builder {
    private final Platform platform;
    private ServiceParser serviceParser;
    private String baseUrl;
    private final List<Converter.Factory> converterFactories = new ArrayList<>();
    private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
    private @Nullable Executor callbackExecutor;
    private boolean validateEagerly;

    Builder(Platform platform, ServiceParser serviceParser) {
      this.platform = platform;
      this.serviceParser = serviceParser;
    }

    public Builder(ServiceParser serviceParser) {
      this(Platform.get(), serviceParser);
    }

    Builder(Retrofit retrofit) {
      platform = Platform.get();
      baseUrl = retrofit.baseUrl;

      converterFactories.addAll(retrofit.converterFactories);
      // Remove the default BuiltInConverters instance added by build().
      converterFactories.remove(0);

      // Do not add the default, platform-aware call adapters added by build().
      for (int i = 0,
          size = retrofit.callAdapterFactories.size() - platform.defaultCallAdapterFactoriesSize();
          i < size; i++) {
        callAdapterFactories.add(retrofit.callAdapterFactories.get(i));
      }

      callbackExecutor = retrofit.callbackExecutor;
      validateEagerly = retrofit.validateEagerly;
      serviceParser = retrofit.serviceParser;
    }


    public Builder baseUrl(String baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      this.baseUrl = baseUrl;
      return this;
    }

    /** Add converter factory for serialization and deserialization of objects. */
    public Builder addConverterFactory(Converter.Factory factory) {
      converterFactories.add(checkNotNull(factory, "factory == null"));
      return this;
    }

    /**
     * Add a call adapter factory for supporting service method return types other than {@link
     * Call}.
     */
    public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
      callAdapterFactories.add(checkNotNull(factory, "factory == null"));
      return this;
    }

    /**
     * The executor on which {@link Callback} methods are invoked when returning {@link Call} from
     * your service method.
     * <p>
     * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
     * return types}.
     */
    public Builder callbackExecutor(Executor executor) {
      this.callbackExecutor = checkNotNull(executor, "executor == null");
      return this;
    }

    /** Returns a modifiable list of call adapter factories. */
    public List<CallAdapter.Factory> callAdapterFactories() {
      return this.callAdapterFactories;
    }

    /** Returns a modifiable list of converter factories. */
    public List<Converter.Factory> converterFactories() {
      return this.converterFactories;
    }

    /**
     * When calling {@link #create} on the resulting {@link Retrofit} instance, eagerly validate
     * the configuration of all methods in the supplied interface.
     */
    public Builder validateEagerly(boolean validateEagerly) {
      this.validateEagerly = validateEagerly;
      return this;
    }

    /**
     * Create the {@link Retrofit} instance using the configured values.
     * <p>
     */
    public Retrofit build() {
//      if (baseUrl == null) {
//        throw new IllegalStateException("Base URL required.");
//      }

      Executor callbackExecutor = this.callbackExecutor;
      if (callbackExecutor == null) {
        callbackExecutor = platform.defaultCallbackExecutor();
      }

      // Make a defensive copy of the adapters and add the default Call adapter.
      List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
      callAdapterFactories.addAll(platform.defaultCallAdapterFactories(callbackExecutor));

      // Make a defensive copy of the converters.
      List<Converter.Factory> converterFactories =
          new ArrayList<>(1 + this.converterFactories.size());

      // Add the built-in converter factory first. This prevents overriding its behavior but also
      // ensures correct behavior when using converters that consume all types.
//      converterFactories.add(new BuiltInConverters());
      converterFactories.addAll(this.converterFactories);

      return new Retrofit(serviceParser, baseUrl, unmodifiableList(converterFactories),
          unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
    }
  }
}

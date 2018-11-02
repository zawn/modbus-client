/*
 * Copyright (C) 2015 Square, Inc.
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Array;


public abstract class ParameterHandler<T> {
    public abstract void apply(ByteBuf builder, @Nullable T value) throws IOException;

    public final ParameterHandler<Iterable<T>> iterable() {
        return new ParameterHandler<Iterable<T>>() {
            @Override
            public void apply(ByteBuf builder, @Nullable Iterable<T> values)
                    throws IOException {
                if (values == null) return; // Skip null values.

                for (T value : values) {
                    ParameterHandler.this.apply(builder, value);
                }
            }
        };
    }

    public final ParameterHandler<Object> array() {
        return new ParameterHandler<Object>() {
            @Override
            public void apply(ByteBuf builder, @Nullable Object values) throws IOException {
                if (values == null) return; // Skip null values.

                for (int i = 0, size = Array.getLength(values); i < size; i++) {
                    //noinspection unchecked
                    ParameterHandler.this.apply(builder, (T) Array.get(values, i));
                }
            }
        };
    }

    static final class ParameterHandlerImpl<T> extends ParameterHandler<T> {
        private final Converter<T, ByteBuf> valueConverter;

        ParameterHandlerImpl(Converter<T, ByteBuf> valueConverter) {
            this.valueConverter = valueConverter;
        }

        @Override
        public void apply(ByteBuf builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            ByteBuf byteBuf = valueConverter.convert(value);
            if (byteBuf == null) return; // Skip converted but null values

            builder.ensureWritable(byteBuf.readableBytes());
            builder.writeBytes(byteBuf);
        }
    }
}

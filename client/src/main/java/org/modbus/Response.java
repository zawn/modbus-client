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

import java.nio.ByteBuffer;

import javax.annotation.Nullable;

/**
 * An HTTP response.
 */
public final class Response<T> {

    private final ByteBuf rawResponse;
    private final @Nullable
    T body;

    public Response(ByteBuf rawResponse, @Nullable T body) {
        this.rawResponse = rawResponse;
        this.body = body;
    }

    /**
     * The raw response from the HTTP client.
     */
    public ByteBuf raw() {
        return rawResponse;
    }


    /**
     * The deserialized response body of a {@linkplain #isSuccessful() successful} response.
     */
    public @Nullable
    T body() {
        return body;
    }

    @Override
    public String toString() {
        return rawResponse.toString();
    }
}

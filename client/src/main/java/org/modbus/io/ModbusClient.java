/*
 * Copyright 2016 Kevin Herron
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.modbus.io;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.modbus.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.requests.ReadHoldingRegistersRequest;
import com.digitalpetri.modbus.requests.WriteMultipleRegistersRequest;
import com.digitalpetri.modbus.responses.ReadHoldingRegistersResponse;
import com.digitalpetri.modbus.responses.WriteMultipleRegistersResponse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

public class ModbusClient {

    private static final Logger logger = LoggerFactory.getLogger(ModbusClient.class);

    /**
     * 一次读写的最大数量
     */
    private static final int MaxRegisterCount = 5 * 20;
    private static final byte[] ZEROS = new byte[MaxRegisterCount];

    ModbusTcpMaster master;

    public ModbusClient(ModbusTcpMaster master) {
        this.master = master;
    }

    public void write(int address, int quantity, ByteBuf byteBuf,
                      int unitId) throws ExecutionException, InterruptedException {
        byte[] dest = new byte[byteBuf.readableBytes()];
        byteBuf.getBytes(0, dest);
        write(address, quantity, dest, unitId);
    }

    /**
     * 写Modbus，注意bytes.length应为偶数。
     */
    public void write(int address, int quantity, byte[] bytes, int unitId)
            throws ExecutionException, InterruptedException {
        logger.debug("write() called with: address = [" + address + "], unitId = [" + unitId + "], bytes = [" + bytes + "]");
        if (logger.isDebugEnabled()) {
            System.out.println(HexDump.dumpHexString(bytes));
        }
        // modbus协议，一次传输数量有限，如果过多，需要分批传输
        int readableSize = bytes.length;
        int internalAdress = address;
        while (readableSize > 0) {
            int offset = bytes.length - readableSize;
            int length = readableSize > MaxRegisterCount ? MaxRegisterCount : readableSize;
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes, offset, length);
            int tcpWrite = internalModbusTcpWrite(internalAdress, byteBuf, unitId);
            internalAdress = internalAdress + tcpWrite;
            readableSize = readableSize - length;
        }
        // bytes长度不足使用0覆写.
        int zeroFill = quantity * 2 - bytes.length;
        readableSize = zeroFill;
        while (readableSize > 0) {
            int length = readableSize > MaxRegisterCount ? MaxRegisterCount : readableSize;
            ByteBuf byteBuf = Unpooled.wrappedBuffer(ZEROS, 0, length);
            int tcpWrite = internalModbusTcpWrite(internalAdress, byteBuf, unitId);
            internalAdress = internalAdress + tcpWrite;
            readableSize = readableSize - length;
        }
        logger.debug("write complete.");
    }

    /**
     * 向Modbus写入数据。
     */
    private int internalModbusTcpWrite(int address, ByteBuf bytes, int unitId)
            throws ExecutionException, InterruptedException {
        logger.debug("internalModbusTcpWrite() called with: address = [" + address + "], bytes = [" + bytes + "], unitId = [" + unitId + "]");
        CompletableFuture<WriteMultipleRegistersResponse> future =
                master.sendRequest(new WriteMultipleRegistersRequest(address, bytes.readableBytes() / 2, bytes),
                        unitId);
        WriteMultipleRegistersResponse response = future.get();
        int distQuantity = response.getQuantity();
        System.out.println("Write Response: " + distQuantity);
        System.out.println("future : " + Thread.currentThread().getName());
        ReferenceCountUtil.release(response);
        return distQuantity;
    }

    /**
     * 从组态中读取内容
     */
    public byte[] read(int startAddress, int registerCount, int unitId)
            throws ExecutionException, InterruptedException {
        logger.debug("read() called with: startAddress = [" + startAddress + "], registerCount = [" + registerCount + "], unitId = [" + unitId + "]");
        ByteBuf byteBuf = Unpooled.buffer(registerCount * 2);
        // modbus协议，一次传输数量有限，如果过多，需要分批传输
        int readableSize = registerCount;
        while (readableSize > 0) {
            int address = startAddress + (registerCount - readableSize);
            int quantity = readableSize > MaxRegisterCount ? MaxRegisterCount : readableSize;
            byte[] bytes = internalModbusTcpRead(address, quantity, unitId);
            byteBuf.writeBytes(bytes);
            readableSize = readableSize - quantity;
        }
        if (logger.isDebugEnabled()) {
            System.out.println(HexDump.dumpHexString(byteBuf.array()));
        }
        return byteBuf.array();
    }


    /**
     * 从Modbus读取指定的字符。
     */
    private byte[] internalModbusTcpRead(int address, int quantity, int unitId)
            throws ExecutionException, InterruptedException {
        logger.debug("internalModbusTcpRead() called with: address = [" + address + "], quantity = [" + quantity + "], unitId = [" + unitId + "]");
        CompletableFuture<ReadHoldingRegistersResponse> future =
                master.sendRequest(new ReadHoldingRegistersRequest(address, quantity), unitId);
        ReadHoldingRegistersResponse response = future.get();
        ByteBuf registers = response.getRegisters();
        byte[] array = new byte[registers.readableBytes()];
        registers.readBytes(array);
        ReferenceCountUtil.release(response);
        return array;
    }
}

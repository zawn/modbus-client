package org.modbus.jackson;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.modbus.util.HexDump;
import org.modbus.client.pojo.ModbusMedicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.Ints;

/**
 * @author zhangzhenli
 */
public class ModbusSerializerTest {

    private static final Logger logger = LoggerFactory.getLogger(ModbusSerializerTest.class);

    @Test
    public void testByteConvert() {
        logger.debug("testByteConvert() called");

        byte b = (byte) 0xFF;
        BigInteger bigInteger = BigInteger.valueOf(b);
        logger.debug(HexDump.dumpHexString(bigInteger.toByteArray()));

        boolean b1 = true;
//        bigInteger = BigInteger.valueOf(b1);
        logger.debug(HexDump.dumpHexString(bigInteger.toByteArray()));

        boolean b2 = false;
        bigInteger = BigInteger.valueOf(b);
        logger.debug(HexDump.dumpHexString(bigInteger.toByteArray()));

        logger.debug(HexDump.dumpHexString(ByteBuffer.allocate(4).put(b).array()));
//        logger.debug(HexDump.dumpHexString(ByteBuffer.allocate(4).put(b1).array()));
        System.out.println("-----");
        logger.debug(HexDump.dumpHexString(Ints.toByteArray(32767)));
        logger.debug(HexDump.dumpHexString(ByteBuffer.allocate(4).putInt(32767).array()));
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        logger.debug(HexDump.dumpHexString(byteBuffer.putShort((short) 32767).array()));
    }


    @Test
    public void serialize() throws JsonProcessingException {
        ModbusMapper modbusMapper = new ModbusMapper();
        List<ModbusMedicine> list = new ArrayList<>();
        list.add(new ModbusMedicine("大黄"));
        list.add(new ModbusMedicine("枇杷叶"));
        list.add(new ModbusMedicine("三七"));
        list.add(new ModbusMedicine("西瓜"));
        list.add(new ModbusMedicine("鸡血藤"));
        byte[] bytes = modbusMapper.writeValueAsBytes(list);


        System.out.println(HexDump.dumpHexString(bytes));
    }
}
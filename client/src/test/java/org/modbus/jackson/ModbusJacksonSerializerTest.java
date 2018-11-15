package org.modbus.jackson;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;
import org.modbus.client.pojo.ModbusMedicine;
import org.modbus.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangzhenli
 */
public class ModbusJacksonSerializerTest {

    private static final Logger logger = LoggerFactory.getLogger(ModbusJacksonSerializerTest.class);

    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static byte[] leIntToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }
    @Test
    public void testByteConvert() {
        logger.debug("testByteConvert() called");

        byte b = (byte) 0xFF;
        byte[] bytes = {127,127};
        logger.debug(HexDump.dumpHexString(bytes));
        final ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int anInt = bb.getShort();
        logger.debug(anInt+"  ");
        logger.debug(HexDump.dumpHexString(bb.array()));

        BigInteger bigInteger = BigInteger.valueOf(b);
        logger.debug(bigInteger.toString());
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
//        logger.debug(HexDump.dumpHexString(Ints.toByteArray(32767)));
        logger.debug(HexDump.dumpHexString(ByteBuffer.allocate(4).putInt(32767).array()));
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        ByteBuffer aShort = byteBuffer.putShort((short) 32767);
        logger.debug(HexDump.dumpHexString(aShort.array()));
    }


    @Test
    public void serialize() throws IOException {
        ModbusMapper modbusMapper = new ModbusMapper();
        List<ModbusMedicine> list = new ArrayList<>();
        list.add(new ModbusMedicine("大黄"));
        list.add(new ModbusMedicine("枇杷叶"));
        list.add(new ModbusMedicine("三七"));
        list.add(new ModbusMedicine("西瓜"));
        list.add(new ModbusMedicine("鸡血藤"));
        byte[] bytes = modbusMapper.writeValueAsBytes(list);

        JavaType t = modbusMapper.getTypeFactory().constructParametricType(List.class,
                ModbusMedicine.class);
        System.out.println(HexDump.dumpHexString(bytes));

        JavaType valueType = t;
//        ModbusParser modbusParser = modbusMapper.getFactory().createParser(bytes, valueType);
//        Object readValue = modbusMapper.readerFor(valueType).readValue(modbusParser);
//        modbusMapper.readerFor(valueType).readValue(bytes);
        List<ModbusMedicine> o = modbusMapper.readValue(bytes, valueType);

        System.out.println(o);
    }
}
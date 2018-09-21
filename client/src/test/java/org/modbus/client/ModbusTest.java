package org.modbus.client;

import java.util.List;

import org.junit.Test;
import org.modbus.client.pojo.Medicine;

/**
 * @author zhangzhenli
 */
public class ModbusTest {

    @Test
    public void test() {
        Modbus modbus = new Modbus.Builder().build();
        KwModbusServer kwModbusServers = modbus.create(KwModbusServer.class);
        List<Medicine> medicineList = kwModbusServers.getMedicineList();
    }
}
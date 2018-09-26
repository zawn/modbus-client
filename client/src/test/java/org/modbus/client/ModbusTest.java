package org.modbus.client;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.modbus.Call;
import org.modbus.ModbusConverterFactory;
import org.modbus.ModbusServiceParser;
import org.modbus.Response;
import org.modbus.Retrofit;
import org.modbus.client.pojo.Medicine;

/**
 * @author zhangzhenli
 */
public class ModbusTest {

    @Test
    public void test() throws IOException {
        String baseUrl = "192.168.10.138:502";
        Retrofit modbus = new Retrofit.Builder(new ModbusServiceParser())
                .baseUrl(baseUrl)
                .addConverterFactory(new ModbusConverterFactory())
                .build();
        KwModbusServer kwModbusServers = modbus.create(KwModbusServer.class);
        Call<List<Medicine>> call = kwModbusServers.getMedicineList();
        Response<List<Medicine>> execute = call.execute();
        List<Medicine> body = execute.body();
    }
}
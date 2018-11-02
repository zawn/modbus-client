package org.modbus.client;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;

import org.junit.Test;
import org.modbus.ModbusCommunication;
import org.modbus.ModbusConverterFactory;
import org.modbus.ModbusServiceParser;
import org.modbus.Retrofit;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author zhangzhenli
 */
public class ModbusTest {

    public static class Person {
        String name;
        int age;
        String address;
    }

    @Test
    public void testJackson() throws IOException {
        KwRobot kwRobot = new KwRobot();
        kwRobot.id = 11;
        kwRobot.name = "大麦一组";
        InetAddress ip = InetAddress.getByName("192.168.1.104");
        byte[] address = ip.getAddress();
        kwRobot.ip = (ip);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();
        String jsonString = objectWriter.writeValueAsString(kwRobot);

        JsonNode jsonNode = mapper.readTree(
                "{\"扩展信息\":\"清肺止咳，凉血止血。\",\"主治\":\"咽喉肿痛，肺热咳嗽，燥咳，咯血，吐血。\",\"采收加工\":\"春、夏季采收，洗净，切碎晒干。\",\"用法用量\":\"内服：煎汤6-15g。\"}");
//        kwRobot.setExpand((ObjectNode) jsonNode);
//        kwRobotMapper.insert(kwRobot);
        System.out.println("ddd");
    }

    @Test
    public void test() throws IOException {
        String baseUrl = "192.168.10.138";

        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(baseUrl)
                .setPort(502).setTimeout(Duration.ofSeconds(6))
                .build();
        ModbusTcpMaster modbusTcpMaster = new ModbusTcpMaster(config);
        Retrofit modbus = new Retrofit.Builder(
                new ModbusServiceParser(new ModbusCommunication(modbusTcpMaster)))
                .baseUrl(baseUrl)
                .addConverterFactory(new ModbusConverterFactory())
                .build();
//        KwModbusServer kwModbusServers = modbus.create(KwModbusServer.class);
//        Call<List<Medicine>> call = kwModbusServers.getMedicineList();
//        Response<List<Medicine>> execute = call.execute();
//        List<Medicine> body = execute.body();
    }
}
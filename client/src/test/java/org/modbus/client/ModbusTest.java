package org.modbus.client;

import com.digitalpetri.modbus.master.ModbusTcpMaster;
import com.digitalpetri.modbus.master.ModbusTcpMasterConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.modbus.Call;
import org.modbus.ModbusServiceParser;
import org.modbus.Response;
import org.modbus.Retrofit;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;
import org.modbus.client.pojo.ModbusMedicine;
import org.modbus.client.pojo.ModbusStatus;
import org.modbus.client.pojo.ModbusWeight;
import org.modbus.client.pojo.PrescriptionProfile;
import org.modbus.converter.ModbusConverterFactory;
import org.modbus.io.ModbusClient;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * @author zhangzhenli
     */
    public interface ModbusRobotService {

        /**
         * 读取药品名称.
         *
         * @return
         */
        @READ(start = 2300, quantity = 250)
        Call<List<ModbusMedicine>> getMedicineList();

        /**
         * 写入药方名称信息.
         *
         * @param list
         * @return
         */
        @WRITE(start = 2300, end = 2549)
        Call<Void> putMedicineList(List<ModbusMedicine> list);

        /**
         * 写入药方重量信息.
         *
         * @param list
         * @return
         */
        @WRITE(start = 2601, end = 2650)
        Call<Void> putMedicineWeight(List<ModbusWeight> list);

        /**
         * 写入药方属性信息.
         *
         * @param profile
         * @return
         */
        @WRITE(start = 2700, quantity = 12)
        Call<Void> putSendStatus(PrescriptionProfile profile);

        /**
         * 读取组态状态.
         *
         * @return
         */
        @READ(start = 2720, quantity = 4)
        Call<ModbusStatus> getKwStatus();

        /**
         * 写入药方属性信息.
         *
         * @param state
         * @return
         */
        @WRITE(start = 2721, quantity = 1)
        Call<Void> putSendStatus(short state);

        /**
         * 读取缺料信息
         *
         * @return
         */
        @READ(start = 3001, end = 3250)
        Call<List<ModbusStatus>> getMedicineStatusList();
    }


    @Test
    public void test() throws IOException {
        String baseUrl = "192.168.10.200";

        ModbusTcpMasterConfig config = new ModbusTcpMasterConfig.Builder(baseUrl)
                .setPort(502).setTimeout(Duration.ofSeconds(6))
                .build();
        ModbusTcpMaster modbusTcpMaster = new ModbusTcpMaster(config);
        Retrofit modbus = new Retrofit.Builder(
                new ModbusServiceParser(new ModbusClient(modbusTcpMaster)))
                .baseUrl(baseUrl)
                .addConverterFactory(new ModbusConverterFactory())
                .build();
        ModbusRobotService kwModbusServers = modbus.create(ModbusRobotService.class);

//        List<ModbusMedicine> list = new ArrayList<>();
//        list.add(new ModbusMedicine("大黄"));
//        list.add(new ModbusMedicine("枇杷叶"));
//        list.add(new ModbusMedicine("三七"));
//        list.add(new ModbusMedicine("西瓜"));
//        list.add(new ModbusMedicine("鸡血藤"));
//
//        Call<Void> voidCall = kwModbusServers.putMedicineList(list);
//        Response<Void> execute1 = voidCall.execute();
//
//        Call<List<ModbusMedicine>> call = kwModbusServers.getMedicineList();
//        Response<List<ModbusMedicine>> execute = call.execute();
//        List<ModbusMedicine> body = execute.body();

//        ModbusStatus modbusStatus = kwModbusServers.getKwStatus().execute().body();
//        System.out.println(modbusStatus);

        Call<Void> voidCall1 = kwModbusServers.putSendStatus((short) 1);
        voidCall1.execute();
    }
}
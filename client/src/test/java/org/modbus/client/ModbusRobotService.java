package org.modbus.client;

import java.util.List;

import org.modbus.Call;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;
import org.modbus.client.pojo.ModbusMedicine;
import org.modbus.client.pojo.ModbusStatus;
import org.modbus.client.pojo.ModbusWeight;
import org.modbus.client.pojo.PrescriptionProfile;

/**
 * @author zhangzhenli
 */
public interface ModbusRobotService {

    /**
     * 读取药品名称.
     *
     * @return
     */
    @READ(start = 1000, end = 2200)
    Call<List<ModbusMedicine>> getMedicineList();

    /**
     * 写入药方名称信息.
     *
     * @param list
     * @return
     */
    @WRITE(start = 2300, end = 2545)
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
    Call<Void> putPrescription(PrescriptionProfile profile);

    /**
     * 读取组态状态.
     *
     * @return
     */
    @READ(start = 2720, quantity = 4)
    Call<ModbusStatus> getKwStatus();

    /**
     * 读取缺料信息
     *
     * @return
     */
    @READ(start = 3001, end = 3250)
    Call<List<ModbusStatus>> getMedicineStatusList();
}

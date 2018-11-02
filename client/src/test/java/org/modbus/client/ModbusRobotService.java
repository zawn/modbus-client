package com.appunity.kw.modbus.service;

import com.appunity.kw.modbus.pojo.ModbusMedicine;
import com.appunity.kw.modbus.pojo.ModbusWeight;
import com.appunity.kw.modbus.pojo.PrescriptionProfile;
import org.modbus.Call;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;

import java.util.List;

/**
 * @author zhangzhenli
 */
public interface ModbusRobotService {

    @READ(start = 1000, end = 2200)
    Call<List<ModbusMedicine>> getMedicineList();

    @READ(start = 3001, end = 3250)
    Call<List<ModbusMedicine>> getMedicineStatusList();

    @WRITE(start = 2300, end = 2545)
    Call<Void> putMedicineList(List<ModbusMedicine> list);

    @WRITE(start = 2300, end = 2545)
    Call<Void> putMedicineWeight(List<ModbusWeight> list);

    @WRITE(start = 2700, quantity = 12)
    Call<Void> putPrescription(PrescriptionProfile profile);

    @READ(start = 2720, quantity = 4)
    Call<List<Integer>> getKwStatus();
}

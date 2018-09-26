package org.modbus.client;

import java.util.List;

import org.modbus.Call;
import org.modbus.annotation.READ;
import org.modbus.annotation.WRITE;
import org.modbus.client.pojo.KwStatus;
import org.modbus.client.pojo.Medicine;
import org.modbus.client.pojo.MedicineWeight;
import org.modbus.client.pojo.Prescription;

/**
 * @author zhangzhenli
 */
public interface KwModbusServer {

    @READ(start = 1000, end = 2200)
    Call<List<Medicine>> getMedicineList();

    @READ(start = 3001, end = 3250)
    Call<List<Medicine>> getMedicineStatusList();

    @WRITE(start = 2300, end = 2545)
    Call<Void> putMedicineList(List<Medicine> list);

    @WRITE(start = 2300, end = 2545)
    Call<Void> putMedicineWeight(List<MedicineWeight> list);

    @WRITE(start = 2700, quantity = 12)
    Call<Void> putPrescription(Prescription list);

    @READ(start = 2720, quantity = 4)
    Call<List<KwStatus>> getKwStatus();
}

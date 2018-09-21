package org.modbus.client;

import java.util.List;

import org.modbus.client.pojo.KwStatus;
import org.modbus.client.pojo.Medicine;
import org.modbus.client.pojo.MedicineWeight;
import org.modbus.client.pojo.Prescription;
import org.modbus.server.READ;
import org.modbus.server.WRITE;

/**
 * @author zhangzhenli
 */
public interface KwModbusServer {

    @READ(start = 1000, end = 2200)
    List<Medicine> getMedicineList();

    @READ(start = 3001, end = 3250)
    List<Medicine> getMedicineStatusList();

    @WRITE(start = 2300, end = 2545)
    void putMedicineList(List<Medicine> list);

    @WRITE(start = 2300, end = 2545)
    void putMedicineWeight(List<MedicineWeight> list);

    @WRITE(start = 2700, quantity = 12)
    void putPrescription(Prescription list);

    @READ(start = 2720, quantity = 4)
    List<KwStatus> getKwStatus();
}

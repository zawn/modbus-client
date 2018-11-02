package org.modbus.client.pojo;

import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class ModbusStatus {
    @Quantity(1)
    int robotReady;
    @Quantity(1)
    int prescriptionSend;
    @Quantity(1)
    int medicineChange;
    @Quantity(1)
    int medicineLack;

    public int getRobotReady() {
        return robotReady;
    }

    public void setRobotReady(int robotReady) {
        this.robotReady = robotReady;
    }

    public int getPrescriptionSend() {
        return prescriptionSend;
    }

    public void setPrescriptionSend(int prescriptionSend) {
        this.prescriptionSend = prescriptionSend;
    }

    public int getMedicineChange() {
        return medicineChange;
    }

    public void setMedicineChange(int medicineChange) {
        this.medicineChange = medicineChange;
    }

    public int getMedicineLack() {
        return medicineLack;
    }

    public void setMedicineLack(int medicineLack) {
        this.medicineLack = medicineLack;
    }
}

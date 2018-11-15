package org.modbus.client.pojo;

import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class ModbusStatus {
    @Quantity(1)
    int robotReady;         // 设备状态

    @Quantity(1)
    int prescriptionSend;   // 服务器药方发送标志

    @Quantity(1)
    int medicineChange;     // 药名信息更新

    @Quantity(1)
    int medicineLack;       // 缺料提示


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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModbusStatus{");
        sb.append("robotReady=").append(robotReady);
        sb.append(", prescriptionSend=").append(prescriptionSend);
        sb.append(", medicineChange=").append(medicineChange);
        sb.append(", medicineLack=").append(medicineLack);
        sb.append('}');
        return sb.toString();
    }
}

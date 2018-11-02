package org.modbus.client.pojo;

import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class ModbusWeight {

    @Quantity(1)
    int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}

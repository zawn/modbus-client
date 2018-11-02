package org.modbus.client.pojo;

import org.modbus.annotation.ByteCharset;
import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class ModbusMedicine {

    @Quantity(5)
    @ByteCharset("GB18030")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

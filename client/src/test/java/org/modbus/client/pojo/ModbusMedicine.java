package org.modbus.client.pojo;

import java.util.StringJoiner;

import org.modbus.annotation.CharsetName;
import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class ModbusMedicine {

    @Quantity(5)
    @CharsetName("GB18030")
    String name;

    public ModbusMedicine() {
    }

    public ModbusMedicine(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModbusMedicine)) return false;

        ModbusMedicine that = (ModbusMedicine) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ModbusMedicine.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .toString();
    }
}

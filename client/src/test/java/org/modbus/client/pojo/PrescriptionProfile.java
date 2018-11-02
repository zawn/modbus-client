package org.modbus.client.pojo;

import org.modbus.annotation.ByteCharset;
import org.modbus.annotation.Quantity;

/**
 * @author zhangzhenli
 */
public class PrescriptionProfile {

    //流水号
    @Quantity(2)
    long serialNumber;
    //姓名
    @Quantity(5)
    @ByteCharset("GB2312")
    String patient;
    //帖数
    int count;
    //属性
    Integer requirement;
    //干配药抓药时间限制
    long timestamp1;
    //代煎药抓药时间限制
    long timestamp2;

    public long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getRequirement() {
        return requirement;
    }

    public void setRequirement(Integer requirement) {
        this.requirement = requirement;
    }

    public long getTimestamp1() {
        return timestamp1;
    }

    public void setTimestamp1(long timestamp1) {
        this.timestamp1 = timestamp1;
    }

    public long getTimestamp2() {
        return timestamp2;
    }

    public void setTimestamp2(long timestamp2) {
        this.timestamp2 = timestamp2;
    }
}

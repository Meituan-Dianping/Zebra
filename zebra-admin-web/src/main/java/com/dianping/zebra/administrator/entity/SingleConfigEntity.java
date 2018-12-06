package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by taochen on 2018/11/10.
 */
@XmlRootElement(name = "singleConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class SingleConfigEntity {
    public SingleConfigEntity() {

    }
    public SingleConfigEntity(String name, int writeWeight, int readWeight, int active) {
        this.name = name;
        this.writeWeight = writeWeight;
        this.readWeight = readWeight;
        this.active = active;
    }
    private String name;

    private int writeWeight;

    private int readWeight;

    private int active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWriteWeight() {
        return writeWeight;
    }

    public void setWriteWeight(int writeWeight) {
        this.writeWeight = writeWeight;
    }

    public int getReadWeight() {
        return readWeight;
    }

    public void setReadWeight(int readWeight) {
        this.readWeight = readWeight;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }
}

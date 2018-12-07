package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by taochen on 2018/11/13.
 */
@XmlRootElement(name = "dimensionConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class DimensionConfig {

    private String dbRule;

    private String dbIndexes;

    private String tbRule;

    private String tbSuffix;

    private boolean tbSuffixZeroPadding;

    private boolean isMaster;

    public String getDbRule() {
        return dbRule;
    }

    public void setDbRule(String dbRule) {
        this.dbRule = dbRule;
    }

    public String getDbIndexes() {
        return dbIndexes;
    }

    public void setDbIndexes(String dbIndexes) {
        this.dbIndexes = dbIndexes;
    }

    public String getTbRule() {
        return tbRule;
    }

    public void setTbRule(String tbRule) {
        this.tbRule = tbRule;
    }

    public String getTbSuffix() {
        return tbSuffix;
    }

    public void setTbSuffix(String tbSuffix) {
        this.tbSuffix = tbSuffix;
    }

    public boolean getTbSuffixZeroPadding() {
        return tbSuffixZeroPadding;
    }

    public void setTbSuffixZeroPadding(boolean tbSuffixZeroPadding) {
        this.tbSuffixZeroPadding = tbSuffixZeroPadding;
    }

    public boolean getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(boolean master) {
        isMaster = master;
    }
}

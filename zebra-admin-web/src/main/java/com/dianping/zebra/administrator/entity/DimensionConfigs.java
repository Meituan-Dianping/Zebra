package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by taochen on 2018/11/13.
 */
@XmlRootElement(name = "dimensionConfigs")
@XmlAccessorType(XmlAccessType.FIELD)
public class DimensionConfigs {

    @XmlElement(name = "dimensionConfigs")
    private List<DimensionConfig> dimensionConfigs;


    public List<DimensionConfig> getDimensionConfig() {
        return dimensionConfigs;
    }

    public void setDimensionConfig(List<DimensionConfig> dimensionConfigs) {
        this.dimensionConfigs = dimensionConfigs;
    }
}

package com.dianping.zebra.administrator.entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by taochen on 2018/11/10.
 */

@XmlRootElement(name = "groupConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupConfigEntity {

    @XmlElement(name = "singleConfig")
    private List<SingleConfigEntity> singleConfigs;

    public List<SingleConfigEntity> getSingleConfigs() {
        return singleConfigs;
    }

    public void setSingleConfigs(List<SingleConfigEntity> singleConfigs) {
        this.singleConfigs = singleConfigs;
    }
}

package com.zkhf.epmis.platform.ent.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class RegionEnt {

    /**
     * 所在地区（地区选择）
     * id1,id2...
     */
    private String region;
    private String regionDesc;

    /** 子节点 */
    private List<Object> subList;
}

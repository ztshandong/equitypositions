package com.zhangtao.entity;

import lombok.Getter;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:48
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Getter
public enum SecurityCodeEnum {
    // TODO 这个枚举类可以删除，只是为了单元测试方便
    REL(1, "REL"),
    ITC(2, "ITC"),
    INF(3, "INF");

    private Integer intValue;
    private String strValue;

    SecurityCodeEnum(Integer intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }
}

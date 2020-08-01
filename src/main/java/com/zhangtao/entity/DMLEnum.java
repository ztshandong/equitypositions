package com.zhangtao.entity;

import lombok.Getter;

/**
 * @param :
 * @author :  zhangtao
 * @createDate :  2020/8/1 09:48
 * @return :
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Getter
public enum DMLEnum {
    // DML类型
    INSERT(1, "INSERT"),
    UPDATE(2, "UPDATE"),
    CANCEL(3, "CANCEL");

    private Integer intValue;
    private String strValue;

    DMLEnum(Integer intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }
}

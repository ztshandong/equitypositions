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
public enum OptEnum {
    // 操作类型
    BUY(1, "BUY"),
    SELL(2, "SELL");

    private Integer intValue;
    private String strValue;

    OptEnum(Integer intValue, String strValue) {
        this.intValue = intValue;
        this.strValue = strValue;
    }
}

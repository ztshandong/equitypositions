package com.zhangtao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author :  张涛
 * @version :  1.0
 * @createDate :  2020/8/1 09:48
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@ApiModel("返回信息")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
@ToString(callSuper = true)
public class ResponseBean {

    // http 状态码
    @ApiModelProperty("状态码")
    private int code;

    // 返回信息成功还是失败
    @ApiModelProperty(value = "成功失败", allowableValues = "success,fail")
    private String msg;

    // 返回的数据
    @ApiModelProperty("返回数据")
    private Object data;

}

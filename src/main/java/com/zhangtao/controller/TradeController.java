package com.zhangtao.controller;

import com.zhangtao.entity.ResponseBean;
import com.zhangtao.entity.TradeEntity;
import com.zhangtao.service.TradeServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:51
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemar :
 */
@Api(tags = "TradeController相关接口")
@RestController
// @Validated
//确保service先启动，初始化securityCodeMap
@DependsOn("tradeServiceImpl")
public class TradeController {

    @Autowired
    TradeServiceImpl tradeServiceImpl;

    @ApiOperation("查询position接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tradeEntity", value = "TradeEntity对象", dataType = "TradeEntity对象")
    })
    @PostMapping("/getPosition")
    @ResponseBody
    public ResponseBean updatePosition(@RequestBody @Valid TradeEntity tradeEntity) {
        //TODO 需要验证调用方的合法性（token是否有效），并且可以进一步做数据合法性的验证

        return ResponseBean.builder()
                .code(HttpStatus.OK.value())
                .msg("success")
                .data(tradeServiceImpl.updatePosition(tradeEntity))
                .build();
    }
}

package com.zhangtao.service;

import com.zhangtao.entity.TradeEntity;

import java.util.HashMap;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:51
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
public interface TradeService {
    //更新position数据
    HashMap<String, Integer> updatePosition(TradeEntity tradeEntity);
}

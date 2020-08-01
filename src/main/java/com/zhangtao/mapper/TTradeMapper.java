package com.zhangtao.mapper;

import com.zhangtao.entity.TradeEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @param :
 * @author :  zhangtao
 * @createDate :  2020/8/1 09:49
 * @return :
 * @description :  TTradeMapper
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Repository
public interface TTradeMapper {

    //新增数据存根
    int insert(TradeEntity record);

    //查询数据存根
    TradeEntity select(@Param("tradeId") int tradeId, @Param("version") int version);

}
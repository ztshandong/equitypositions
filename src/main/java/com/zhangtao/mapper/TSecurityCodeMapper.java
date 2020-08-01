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
 * @description :  TSecurityCodeMapper
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Repository
public interface TSecurityCodeMapper {

    //获取SecurityCode
    List<String> getSecurityCode();
}
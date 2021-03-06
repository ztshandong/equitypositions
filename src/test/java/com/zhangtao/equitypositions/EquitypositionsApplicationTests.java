package com.zhangtao.equitypositions;

import com.zhangtao.controller.TradeController;
import com.zhangtao.entity.DMLEnum;
import com.zhangtao.entity.OptEnum;
import com.zhangtao.entity.SecurityCodeEnum;
import com.zhangtao.entity.TradeEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@MapperScan("com.zhangtao.mapper")
@RunWith(SpringRunner.class)
@SpringBootTest
public class EquitypositionsApplicationTests {

    @Autowired
    TradeController tradeController;

    /*
    TransactionID TradeID Version SecurityCode Quantity Insert/Update/Cancel Buy/Sell
    1               1       1       REL         50          INSERT              Buy
    2               2       1       ITC         40          INSERT              Sell
    3               3       1       INF         70          INSERT              Buy
    4               1       2       REL         60          UPDATE              Buy
    5               2       2       ITC         30          CANCEL              Buy
    6               4       1       INF         20          INSERT              Sell
    7               3       2       INF         80          UPDATE              Sell
    8               1       3       REL         80          UPDATE              Sell
    9               3       4       INF         50          CANCEL              Buy
    10              4       2       REL         20          UPDATE              Sell

     */
    @Test
    public void test() {
        TradeEntity tradeEntity = new TradeEntity();

        tradeEntity.setTransactionId(1);
        tradeEntity.setTradeId(1);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.REL.getStrValue());
        tradeEntity.setQuantity(50);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.BUY.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(2);
        tradeEntity.setTradeId(2);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.ITC.getStrValue());
        tradeEntity.setQuantity(40);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(3);
        tradeEntity.setTradeId(3);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(70);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.BUY.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(4);
        tradeEntity.setTradeId(1);
        tradeEntity.setVersion(2);
        tradeEntity.setSecurityCode(SecurityCodeEnum.REL.getStrValue());
        tradeEntity.setQuantity(60);
        tradeEntity.setDml(DMLEnum.UPDATE.getStrValue());
        tradeEntity.setOpt(OptEnum.BUY.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(5);
        tradeEntity.setTradeId(2);
        tradeEntity.setVersion(2);
        tradeEntity.setSecurityCode(SecurityCodeEnum.ITC.getStrValue());
        tradeEntity.setQuantity(30);
        tradeEntity.setDml(DMLEnum.CANCEL.getStrValue());
        tradeEntity.setOpt(OptEnum.BUY.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(6);
        tradeEntity.setTradeId(4);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(7);
        tradeEntity.setTradeId(3);
        tradeEntity.setVersion(2);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(80);
        tradeEntity.setDml(DMLEnum.UPDATE.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(8);
        tradeEntity.setTradeId(1);
        tradeEntity.setVersion(3);
        tradeEntity.setSecurityCode(SecurityCodeEnum.REL.getStrValue());
        tradeEntity.setQuantity(80);
        tradeEntity.setDml(DMLEnum.UPDATE.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(9);
        tradeEntity.setTradeId(3);
        tradeEntity.setVersion(3);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(50);
        tradeEntity.setDml(DMLEnum.CANCEL.getStrValue());
        tradeEntity.setOpt(OptEnum.BUY.getStrValue());
        tradeController.updatePosition(tradeEntity);

        tradeEntity.setTransactionId(10);
        tradeEntity.setTradeId(4);
        tradeEntity.setVersion(2);
        tradeEntity.setSecurityCode(SecurityCodeEnum.REL.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(DMLEnum.UPDATE.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        //下面全是非法数据
        System.out.println("主键冲突测试");
        tradeEntity.setTransactionId(6);
        tradeEntity.setTradeId(4);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        System.out.println("唯一索引冲突测试");
        tradeEntity.setTransactionId(7);
        tradeEntity.setTradeId(4);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        System.out.println("SecurityCode非法测试");
        tradeEntity.setTransactionId(7);
        tradeEntity.setTradeId(5);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode("xxx");
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(DMLEnum.INSERT.getStrValue());
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

        System.out.println("null数据测试，单元测试中@ExceptionHandler无效");
        tradeEntity.setTransactionId(7);
        tradeEntity.setTradeId(6);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml(null);
        tradeEntity.setOpt(null);
        tradeController.updatePosition(tradeEntity);

        System.out.println("DML非法测试");
        tradeEntity.setTransactionId(7);
        tradeEntity.setTradeId(6);
        tradeEntity.setVersion(1);
        tradeEntity.setSecurityCode(SecurityCodeEnum.INF.getStrValue());
        tradeEntity.setQuantity(20);
        tradeEntity.setDml("xxx");
        tradeEntity.setOpt(OptEnum.SELL.getStrValue());
        tradeController.updatePosition(tradeEntity);

    }

}

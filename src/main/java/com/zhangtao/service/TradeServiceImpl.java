package com.zhangtao.service;

import com.zhangtao.entity.DMLEnum;
import com.zhangtao.entity.OptEnum;
import com.zhangtao.entity.TradeEntity;
import com.zhangtao.log.LogUtil;
import com.zhangtao.mapper.TSecurityCodeMapper;
import com.zhangtao.mapper.TTradeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:51
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    TTradeMapper tTradeMapper;

    @Autowired
    TSecurityCodeMapper tSecurityCodeMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    //TODO securityCodeMap当数据变化时应同步更新缓存
    HashMap<String, String> securityCodeMap = new HashMap<>();

    //TODO 下面的几个HashMap数据应该持久化保存，服务启动时重新加载
    //记录position
    HashMap<String, Integer> positionMap = new HashMap<>();

    //记录cancel类型的tradeId与对应的version，应对到达顺序错误，一定是单独的一个容器
    HashMap<Integer, Integer> cancelVersionMap = new HashMap<>();

    //记录update类型的tradeId与对应的version，应对到达顺序错误
    HashMap<Integer, Integer> updateVersionMap = new HashMap<>();

    //记录insert类型的tradeId与对应的version，应对到达顺序错误
    HashMap<Integer, Integer> insertVersionMap = new HashMap<>();

    //TODO 防止多个tradeId相同但version不同的操作，可使用细粒度锁，针对tradeId加锁，但细粒度锁要注意hashmap并发扩容问题
    ReentrantLock versionLock = new ReentrantLock();

    @PostConstruct
    void init() {
        List<String> securityCodes = tSecurityCodeMapper.getSecurityCode();
        for (String securityCode : securityCodes) {
            securityCodeMap.put(securityCode, securityCode);
        }
    }

    @Override
    public HashMap<String, Integer> updatePosition(TradeEntity tradeEntity) {

        //查询本地存根
        TradeEntity transactionsEntity = select(tradeEntity.getTradeId(), tradeEntity.getVersion());

        if (!securityCodeMap.containsKey(tradeEntity.getSecurityCode())) {
            LogUtil.log("非法的SecurityCode");
            return positionMap;
        }
        //TODO 应该先做数据合法性校验再保存，否则会保存非法数据
        //本地存根没有，说明是新数据，即使是迟到的数据，也需要保存，使用数据库索引保证唯一性
        if (transactionsEntity == null && insert(tradeEntity) == 1) {
            //处理position数据，防止数据到达的顺序错误，需要加锁
            versionLock.lock();
            try {
                //查询trade是否被cancel
                Integer cancelVersion = cancelVersionMap.get(tradeEntity.getTradeId());

                boolean b = cancelVersion == null;
                if (!b) {
                    LogUtil.log("已被cancel，不用继续操作");
                    return positionMap;
                }

                //获取最后一次的update操作版本
                Integer updateVersion = updateVersionMap.get(tradeEntity.getTradeId());

                //获取最后一次的insert操作版本
                Integer insertVersion = insertVersionMap.get(tradeEntity.getTradeId());

                //首先处理cancel类型，最简单，数据清零即可
                if (DMLEnum.CANCEL.getStrValue().equals(tradeEntity.getDml())) {
                    //TODO insertVersion == null 应等待一段时间后做二次校验，超过重试次数上限后需做额外处理
                    //当前cancel操作版本号最大时才可以清零
                    //之前没有update或者先于update到达或者是在上次update之后的操作
                    b = (updateVersion == null || updateVersion < tradeEntity.getVersion());
                    //cancel操作先于insert到达，或者上次insert之后的操作
                    b = b & (insertVersion == null || insertVersion < tradeEntity.getVersion());

                    if (b) {
                        //记录trade已被cancel
                        cancelVersionMap.put(tradeEntity.getTradeId(), tradeEntity.getVersion());
                        //position对应的securityCode数据清零
                        positionMap.put(tradeEntity.getSecurityCode(), 0);
                    }
                } else if (DMLEnum.UPDATE.getStrValue().equals(tradeEntity.getDml())) {
                    //然后处理update类型，更新，需要判断是否在cancel之后到达，同时要与之前的update和insert操作对比
                    //之前没有update或者是在上次update之后的操作
                    b = (updateVersion == null || updateVersion < tradeEntity.getVersion());
                    //update操作先于insert到达，或者上次insert之后的操作
                    b = b & (insertVersion == null || insertVersion < tradeEntity.getVersion());

                    if (b) {
                        //更新positionMap的数据
                        positionMap.put(tradeEntity.getSecurityCode(), tradeEntity.getQuantity());
                        //记录此次update的version
                        updateVersionMap.put(tradeEntity.getTradeId(), tradeEntity.getVersion());
                    }
                } else if (DMLEnum.INSERT.getStrValue().equals(tradeEntity.getDml())) {
                    //最后处理insert类型，需要判断是否在cancel之后到达，同时要与之前的update和cancel操作对比

                    //如果在update之后到达也无需操作
                    b = (updateVersion == null);
                    //TODO insert版本号必须为1 可单独记录日志
                    b = b & (tradeEntity.getVersion() == 1);
                    if (b) {
                        Integer oldQuantityObj = positionMap.get(tradeEntity.getSecurityCode());
                        int curQuantity = tradeEntity.getQuantity();
                        int oldQuantity = oldQuantityObj == null ? 0 : oldQuantityObj;
                        if (OptEnum.BUY.getStrValue().equals(tradeEntity.getOpt())) {
                            //更新positionMap的数据
                            positionMap.put(tradeEntity.getSecurityCode(), oldQuantity + curQuantity);
                            //记录此次insert的version
                            insertVersionMap.put(tradeEntity.getTradeId(), tradeEntity.getVersion());
                        } else if (OptEnum.SELL.getStrValue().equals(tradeEntity.getOpt())) {
                            //更新positionMap的数据
                            positionMap.put(tradeEntity.getSecurityCode(), oldQuantity - curQuantity);
                            //记录此次insert的version
                            insertVersionMap.put(tradeEntity.getTradeId(), tradeEntity.getVersion());
                        } else {
                            //TODO 也可以自定义枚举@EnumValidator，或者删除非法数据
                            LogUtil.log("非法OPT操作");
                        }
                    }
                } else {
                    //TODO 也可以自定义枚举@EnumValidator，或者删除非法数据
                    LogUtil.log("非法DML操作");
                }

                System.out.println(tradeEntity.getTransactionId() + ":当前position数据-----------");
                positionMap.forEach((securityCode, quantity) -> {
                    System.out.println(securityCode + " >> " + quantity);
                });
                return positionMap;
            } catch (Exception e) {
                LogUtil.log(e, "发生异常");
            } finally {
                versionLock.unlock();
            }
        }
        return positionMap;
    }

    private int insert(final TradeEntity tradeEntity) {

        int row = (int) transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                try {
                    int i = tTradeMapper.insert(tradeEntity);
                    if (i == 1) {
                        LogUtil.log("保存成功>>>>");
                        LogUtil.log(tradeEntity);
                    }
                    return i;
                } catch (org.springframework.dao.DuplicateKeyException e) {
                    LogUtil.log(e, "已有记录");
                    return -1;
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    LogUtil.log(e, "数据不合法");
                    return -1;
                } catch (RuntimeException e) {
                    LogUtil.log(e, "RuntimeException");
                    return -1;
                } catch (Exception e) {
                    LogUtil.log(e, "Exception");
                    return -1;
                } finally {

                }
            }
        });
        return row;
    }

    private TradeEntity select(int tradeId, int version) {
        TradeEntity tradeEntity = tTradeMapper.select(tradeId, version);
        return tradeEntity;
    }

}

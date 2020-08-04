package com.zhangtao.service;

import com.alibaba.fastjson.JSON;
import com.zhangtao.entity.DMLEnum;
import com.zhangtao.entity.OptEnum;
import com.zhangtao.entity.StrHelper;
import com.zhangtao.entity.TradeEntity;
import com.zhangtao.log.LogUtil;
import com.zhangtao.mapper.TSecurityCodeMapper;
import com.zhangtao.mapper.TTradeMapper;
import com.zhangtao.redis.ZtJedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private ZtJedisUtils jedis;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    //TODO securityCodeMap当数据变化时应同步更新缓存
    HashMap<String, String> securityCodeMap = new HashMap<>();

    HashMap<String, String> dmlMap = new HashMap<>();

    HashMap<String, String> optMap = new HashMap<>();

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

        for (DMLEnum e : DMLEnum.values()) {
            dmlMap.put(e.getStrValue(), e.getStrValue());
        }

        for (OptEnum e : OptEnum.values()) {
            optMap.put(e.getStrValue(), e.getStrValue());
        }
    }

    /**
     * @param tradeEntity :
     * @return :  java.util.HashMap<java.lang.String,java.lang.Integer>
     * @author :  zhangtao
     * @createDate :  2020/8/4 15:40
     * @description :
     * 1.INSERT    根据Buy或Sell增加或减少
     * 2.UPDATE    查找当前tradeId的上一次操作记录，计算差值，然后更新
     * 3.CANCLE    查找当前tradeId的所有操作记录，根据版本号从大到小排序，依次反向操作，遇到UPDATE后退出即可
     * @updateUser :
     * @updateDate :
     * @updateRemark :
     */
    @Override
    public HashMap<String, Integer> updatePosition(TradeEntity tradeEntity) {

        //查询本地存根
        TradeEntity transactionsEntity = select(tradeEntity.getTradeId(), tradeEntity.getVersion());

        //先做数据合法性校验再保存，否则会保存非法数据
        if (!securityCodeMap.containsKey(tradeEntity.getSecurityCode())) {
            LogUtil.log("非法的SecurityCode");
            return positionMap;
        }

        if (!dmlMap.containsKey(tradeEntity.getDml())) {
            LogUtil.log("非法的DML类型");
            return positionMap;
        }

        if (!optMap.containsKey(tradeEntity.getOpt())) {
            LogUtil.log("非法的Opt类型");
            return positionMap;
        }

        //本地存根没有，说明是新数据，即使是迟到的数据，也需要保存，使用数据库索引保证唯一性
        if (transactionsEntity == null && insert(tradeEntity) == 1) {
            //处理position数据，防止数据到达的顺序错误，需要加锁，必要时可使用分布式锁
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

                //处理cancel类型
                if (DMLEnum.CANCEL.getStrValue().equals(tradeEntity.getDml())) {
                    //TODO insertVersion == null 应等待一段时间后做二次校验，超过重试次数上限后需做额外处理
                    //当前cancel操作版本号最大时才可以清零
                    //之前没有update或者先于update到达或者是在上次update之后的操作
                    b = (updateVersion == null || updateVersion < tradeEntity.getVersion());
                    //cancel操作先于insert到达，或者上次insert之后的操作
                    b = b & (insertVersion == null || insertVersion < tradeEntity.getVersion());

                    if (b) {
                        //获取当前SecurityCode对应的Quantity
                        Integer oldQuantityObj = positionMap.get(tradeEntity.getSecurityCode());
                        int oldQuantity = oldQuantityObj == null ? 0 : oldQuantityObj;

                        //TODO 如果数据到达顺序错误，这里拿到的值可能不完整，需要进一步优化
                        //获取当前tradeId的所有历史记录
                        Set<String> set = jedis.smembers(StrHelper.TRADEID + tradeEntity.getTradeId());
                        List<TradeEntity> list = new ArrayList<>();
                        for (String s : set) {
                            list.add(JSON.parseObject(s, TradeEntity.class));
                        }
                        //按照版本号倒序排序
                        list.sort(Comparator.comparing(TradeEntity::getVersion).reversed());

                        //需要处理的差值
                        int diffQuantity = 0;

                        TradeEntity tradeEntity1 = null;
                        for (int i = 0, j = list.size(); i < j; i++) {
                            tradeEntity1 = list.get(i);

                            //如果是insert，则将数据反向操作一遍即可，原来buy的减掉，原来sell的加回去
                            //但如果碰到update，将数据反向操作完成后，不可继续循环，应该退出
                            if (tradeEntity1.getVersion() < tradeEntity.getVersion()) {
                                if (OptEnum.BUY.getStrValue().equals(tradeEntity1.getOpt())) {
                                    diffQuantity -= tradeEntity1.getQuantity();
                                } else {
                                    diffQuantity += tradeEntity1.getQuantity();
                                }
                                if (DMLEnum.UPDATE.getStrValue().equals(tradeEntity1.getDml())) {
                                    break;
                                }
                            }
                        }

                        //记录trade已被cancel
                        cancelVersionMap.put(tradeEntity.getTradeId(), tradeEntity.getVersion());
                        //更新positionMap的数据，旧值加上差值
                        positionMap.put(tradeEntity.getSecurityCode(), oldQuantity + diffQuantity);
                    }
                } else if (DMLEnum.UPDATE.getStrValue().equals(tradeEntity.getDml())) {

                    //处理update类型，更新，需要判断是否在cancel之后到达，同时要与之前的update和insert操作对比
                    //之前没有update或者是在上次update之后的操作
                    b = (updateVersion == null || updateVersion < tradeEntity.getVersion());
                    //update操作先于insert到达，或者上次insert之后的操作
                    b = b & (insertVersion == null || insertVersion < tradeEntity.getVersion());

                    if (b) {
                        //获取当前SecurityCode对应的Quantity
                        Integer oldQuantityObj = positionMap.get(tradeEntity.getSecurityCode());
                        int oldQuantity = oldQuantityObj == null ? 0 : oldQuantityObj;

                        //本次传入的Quantity
                        int curQuantity = tradeEntity.getQuantity();

                        //TODO 如果数据到达顺序错误，这里可能拿不到值，需要进一步优化
                        //获取当前tradeId的上一个版本的历史记录
                        Map<String, String> map = jedis.hgetAll(StrHelper.TRADEID + tradeEntity.getTradeId() + StrHelper.VERSION + (tradeEntity.getVersion() - 1));
                        //上一次操作的Quantity
                        int lastQuantity = 0;
                        try {
                            lastQuantity = Integer.parseInt(map.get(StrHelper.QUANTITY));
                        } catch (Exception e) {

                        }

                        //需要处理的差值
                        int diffQuantity = 0;

                        //如果更改了SecurityCode就不能靠历史记录了，直接增加或减少Quantity
                        //TODO 原来的SecurityCode数据未做处理
                        if (!tradeEntity.getSecurityCode().equals(map.get(StrHelper.SECURITY_CODE))) {
                            if (OptEnum.BUY.getStrValue().equals(tradeEntity.getOpt())) {
                                diffQuantity = curQuantity;
                            } else if (OptEnum.SELL.getStrValue().equals(tradeEntity.getOpt())) {
                                diffQuantity = -curQuantity;
                            } else {
                                LogUtil.log("UPDATE非法OPT操作");
                            }
                        } else {
                            //未更改SecurityCode，分四种情况计算差值
                            //上一次是buy
                            if (OptEnum.BUY.getStrValue().equals(map.get(StrHelper.OPT))) {
                                if (OptEnum.BUY.getStrValue().equals(tradeEntity.getOpt())) {
                                    diffQuantity = curQuantity - lastQuantity;
                                } else if (OptEnum.SELL.getStrValue().equals(tradeEntity.getOpt())) {
                                    diffQuantity = -curQuantity - lastQuantity;
                                } else {
                                    LogUtil.log("UPDATE非法OPT操作");
                                }
                            } else { //上一次是sell
                                if (OptEnum.BUY.getStrValue().equals(tradeEntity.getOpt())) {
                                    diffQuantity = curQuantity + lastQuantity;
                                } else if (OptEnum.SELL.getStrValue().equals(tradeEntity.getOpt())) {
                                    diffQuantity = lastQuantity - curQuantity;
                                } else {
                                    LogUtil.log("UPDATE非法OPT操作");
                                }
                            }
                        }
                        //更新positionMap的数据，旧值加上差值
                        positionMap.put(tradeEntity.getSecurityCode(), oldQuantity + diffQuantity);
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
                            LogUtil.log("INSERT非法OPT操作");
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
        if (row == 1) {
            //TODO 暂时不考虑redis保存失败的情况
            //使用tradeId为key，记录所有历史操作 cancel的时候遍历这个set
            jedis.sadd(StrHelper.TRADEID + tradeEntity.getTradeId(), JSON.toJSONString(tradeEntity));

            //使用tradeId+version为key，记录每一次操作
            //update的时候根据tradeId与version查询这个HashMap
            HashMap<String, String> map = new HashMap<>();
            map.put(StrHelper.SECURITY_CODE, tradeEntity.getSecurityCode() + "");
            map.put(StrHelper.QUANTITY, tradeEntity.getQuantity() + "");
            map.put(StrHelper.DML, tradeEntity.getDml());
            map.put(StrHelper.OPT, tradeEntity.getOpt());
            jedis.hmset(StrHelper.TRADEID + tradeEntity.getTradeId() + StrHelper.VERSION + tradeEntity.getVersion(), map);
        }
        return row;
    }

    private TradeEntity select(int tradeId, int version) {
        TradeEntity tradeEntity = tTradeMapper.select(tradeId, version);
        return tradeEntity;
    }

}

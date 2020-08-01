package com.zhangtao.log;

import lombok.extern.log4j.Log4j2;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:48
 * @description : 日志工具
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@Log4j2
public class LogUtil {
    // TODO 记录日志时可写入文件或mongo
    public static void log(Exception e, Object o) {
        log.error(o);
    }

    public static void log(Object o) {
        log.info(o);
    }
}

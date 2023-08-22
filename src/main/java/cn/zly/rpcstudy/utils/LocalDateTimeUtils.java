package cn.zly.rpcstudy.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author zhanglianyong
 * 2023-08-17 22:33
 */
public class LocalDateTimeUtils {


    public static long getEpochMilli() {
        return LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

}

package com.littlersmall.common;

/**
 * Created by littlersmall on 16/12/2.
 */
public class Constants {
    //一个操作的有效时间(多机部署时避免重复发送) sec
    public static int TIMESTAMP_VALID_TIME = 600;

    //处理时间间隔
    public final static int INTERVAL_MILS = 1;
    public static int CONSUMER_THREAD_COUNT = 1;
}

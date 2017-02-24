package com.littlersmall.biz.event;

import com.google.code.or.binlog.BinlogEventV4;

/**
 * Created by littlersmall on 16/11/28.
 */
public interface EventProcess {
    void process(BinlogEventV4 event);

    Class<?> getEventClass();
}

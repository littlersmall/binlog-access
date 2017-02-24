package com.littlersmall.biz.event;

import com.google.code.or.binlog.BinlogEventV4;
import com.littlersmall.biz.meta.TableMetaCache;
import com.littlersmall.biz.sender.RowDiffMessageSender;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by littlersmall on 16/11/28.
 */
@Slf4j
public class AllEventProcessor {
    private Map<Class, EventProcess> eventProcessMap = new HashMap<>();

    public AllEventProcessor(TableMetaCache tableMetaCache, RowDiffMessageSender messageSender) {
        register(new TableMapEventProcessor(tableMetaCache));
        register(new InsertEventProcessor(tableMetaCache, messageSender));
        register(new UpdateEventProcessor(tableMetaCache, messageSender));
        register(new DeleteEventProcessor(tableMetaCache, messageSender));
    }

    public void process(BinlogEventV4 event) {
        EventProcess eventProcess = eventProcessMap.get(event.getClass());

        if (null != eventProcess) {
            eventProcess.process(event);
        } else {
            log.debug("no process event: " + event);
        }
    }

    public void register(EventProcess eventProcess) {
        eventProcessMap.put(eventProcess.getEventClass(), eventProcess);
    }
}

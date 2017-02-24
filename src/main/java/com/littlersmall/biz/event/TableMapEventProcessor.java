package com.littlersmall.biz.event;

import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.TableMapEvent;
import com.littlersmall.biz.meta.TableMetaCache;

/**
 * Created by littlersmall on 16/11/28.
 */
public class TableMapEventProcessor implements EventProcess {
    private TableMetaCache tableMetaCache;

    public TableMapEventProcessor(TableMetaCache tableMetaCache) {
        this.tableMetaCache = tableMetaCache;
    }

    @Override
    public void process(BinlogEventV4 event) {
        TableMapEvent tableMapEvent = (TableMapEvent) event;
        String tableName = tableMapEvent.getDatabaseName() + "." + tableMapEvent.getTableName();

        tableMetaCache.put("" + tableMapEvent.getTableId(), tableName);
    }

    @Override
    public Class<?> getEventClass() {
        return TableMapEvent.class;
    }
}

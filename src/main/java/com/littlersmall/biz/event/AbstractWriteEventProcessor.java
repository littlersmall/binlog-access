package com.littlersmall.biz.event;

import com.google.code.or.binlog.BinlogEventV4;
import com.google.code.or.binlog.impl.event.AbstractRowEvent;
import com.google.code.or.common.glossary.Column;
import com.google.code.or.common.glossary.column.BlobColumn;
import com.google.code.or.common.glossary.column.StringColumn;
import com.littlersmall.biz.meta.TableMetaCache;
import com.littlersmall.biz.sender.RowDiffMessageSender;
import com.littlersmall.model.TableMeta;
import com.yidian.commerce.common.utils.model.RowDiffModel;

import java.util.List;

/**
 * Created by littlersmall on 16/11/30.
 */
public abstract class AbstractWriteEventProcessor<T extends AbstractRowEvent> implements EventProcess {
    private TableMetaCache tableMetaCache;
    private RowDiffMessageSender messageSender;

    public AbstractWriteEventProcessor(TableMetaCache tableMetaCache, RowDiffMessageSender messageSender) {
        this.tableMetaCache = tableMetaCache;
        this.messageSender = messageSender;
    }

    @Override
    public void process(BinlogEventV4 event) {
        AbstractRowEvent abstractRowEvent = (AbstractRowEvent) event;
        String tableId = "" + abstractRowEvent.getTableId();
        @SuppressWarnings("unchecked")
        List<RowDiffModel> rowDiffModels = buildRowDiffModel(tableId, (T) event);

        messageSender.send(event.getHeader().getPosition(), rowDiffModels);
    }

    abstract List<RowDiffModel> buildRowDiffModel(String tableId, T event);

    protected TableMeta getTableMeta(String tableId, int realColumnCount) {
        TableMeta tableMeta = tableMetaCache.getTableMeta(tableId);

        if (tableMeta.getColumnMetas().size() != realColumnCount) {
            tableMeta = tableMetaCache.rebuild(tableMeta.getTableName());
        }

        return tableMeta;
    }

    protected Object getValue(Column column) {
        //String类型较为特殊
        if (column instanceof StringColumn) {
            return column.toString();
        } else if (column instanceof BlobColumn) { //text类型特殊处理
            return new String((byte[]) column.getValue());
        }
        else {
            return column.getValue();
        }
    }
}

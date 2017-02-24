package com.littlersmall.biz.event;

import com.google.code.or.binlog.impl.event.DeleteRowsEventV2;
import com.littlersmall.biz.meta.TableMetaCache;
import com.littlersmall.biz.sender.RowDiffMessageSender;
import com.littlersmall.model.ColumnMeta;
import com.littlersmall.model.TableMeta;
import com.yidian.commerce.common.utils.model.RowDiffModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by littlersmall on 16/11/30.
 */
public class DeleteEventProcessor extends AbstractWriteEventProcessor<DeleteRowsEventV2> {
    public DeleteEventProcessor(TableMetaCache tableMetaCache, RowDiffMessageSender messageSender) {
        super(tableMetaCache, messageSender);
    }

    @Override
    List<RowDiffModel> buildRowDiffModel(String tableId, DeleteRowsEventV2 event) {
        List<RowDiffModel> rowDiffModels = new ArrayList<>();
        TableMeta tableMeta = getTableMeta(tableId, event.getColumnCount().intValue());

        event.getRows().forEach(row -> {
            RowDiffModel rowDiffModel = new RowDiffModel();

            rowDiffModel.setTableName(tableMeta.getTableName());
            rowDiffModel.setTimestamp(event.getHeader().getTimestamp());
            rowDiffModel.setType(3);

            for (int i = 0; i < tableMeta.getColumnMetas().size(); i++) {
                ColumnMeta columnMeta = tableMeta.getColumnMetas().get(i);
                Object value = getValue(row.getColumns().get(i));

                if (columnMeta.isPk()) {
                    rowDiffModel.getPkColumnName().add(columnMeta.getColumnName());
                    rowDiffModel.getPk().add(value);
                }

                rowDiffModel.getDiffColumns().add(columnMeta.getColumnName());
                rowDiffModel.getPreValue().put(columnMeta.getColumnName(), value);
            }

            rowDiffModels.add(rowDiffModel);
        });

        return rowDiffModels;
    }

    @Override
    public Class<?> getEventClass() {
        return DeleteRowsEventV2.class;
    }
}

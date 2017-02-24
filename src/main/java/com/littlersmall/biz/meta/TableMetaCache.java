package com.littlersmall.biz.meta;

import com.littlersmall.model.TableMeta;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by littlersmall on 16/11/21.
 */
public class TableMetaCache {
    private Map<String, TableMeta> tableMetaMap = new ConcurrentHashMap<>();
    private Map<String, String> tableId2Name = new ConcurrentHashMap<>();
    private TableMetaBuilder tableMetaBuilder;

    public TableMetaCache(JdbcTemplate jdbcTemplate) {
        tableMetaBuilder = new TableMetaBuilder(jdbcTemplate);
    }

    public TableMeta rebuild(String tableId) {
        tableMetaMap.remove(tableId);

        return getTableMeta(tableId);
    }

    public TableMeta getTableMeta(String tableId) {
        TableMeta tableMeta = tableMetaMap.get(tableId);

        if (null == tableMeta) {
            String tableName = getTableName(tableId);
            tableMeta = tableMetaBuilder.getTableMeta(tableName);

            synchronized (this) {
                if (!tableMetaMap.containsKey(tableId)) {
                    tableMetaMap.put(tableId, tableMeta);
                }
            }
        }

        return tableMeta;
    }

    public String getTableName(String tableId) {
        return tableId2Name.get(tableId);
    }

    public void put(String tableId, String tableName) {
        String preTableName = tableId2Name.get(tableId);

        if (null == preTableName
                || !preTableName.equals(tableName)) {
            tableId2Name.put(tableId, tableName);
        }
    }
}

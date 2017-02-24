package com.littlersmall.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by littlersmall on 16/11/21.
 */
@Data
public class TableMeta {
    int tableId;
    String tableName; //dbName.tableName
    List<ColumnMeta> columnMetas = new ArrayList<>();
}

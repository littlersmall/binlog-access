package com.littlersmall.model;

import lombok.Data;

/**
 * Created by littlersmall on 16/11/21.
 */
@Data
public class ColumnMeta {
    String columnName;
    boolean isPk;
    String type;
}

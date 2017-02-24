package com.littlersmall.model;

import lombok.Data;

/**
 * Created by littlersmall on 16/11/25.
 */
@Data
public class BinlogMeta {
    String binlogName;
    long pos;
}

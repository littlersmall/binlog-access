package com.littlersmall.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by littlersmall on 16/11/29.
 */
@Data
public class RowDiffModel {
    long timestamp;
    String tableName;
    List<String> pkColumnName = new ArrayList<>();
    List<Object> pk = new ArrayList<>();
    int type;  //1 新建 //2 更新 //3 删除
    List<String> diffColumns = new ArrayList<>();
    Map<String, Object> preValue = new HashMap<>();
    Map<String, Object> newValue = new HashMap<>();
}

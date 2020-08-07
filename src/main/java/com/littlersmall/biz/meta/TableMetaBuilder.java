package com.littlersmall.biz.meta;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import com.littlersmall.model.ColumnMeta;
import com.littlersmall.model.TableMeta;

/**
 * Created by littlersmall on 16/11/21.
 */
public class TableMetaBuilder {
    private JdbcTemplate jdbcTemplate;

    public TableMetaBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TableMeta getTableMeta(String tableName) {
        TableMeta tableMeta = new TableMeta();
        tableMeta.setTableName(tableName);

        jdbcTemplate.queryForList("desc " + tableName).forEach(column -> {
            ColumnMeta columnMeta = new ColumnMeta();

            columnMeta.setColumnName((String) column.get("Field"));
            columnMeta.setPk("PRI".equals(column.get("Key")));
            columnMeta.setType((String) column.get("Type"));

            tableMeta.getColumnMetas().add(columnMeta);
        });

        return tableMeta;
    }

    public static void main(String[] args) {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/lx_charge");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("root");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(basicDataSource);

        TableMetaBuilder tableMetaBuilder = new TableMetaBuilder(jdbcTemplate);

        System.out.println(tableMetaBuilder.getTableMeta("budget_instruction"));
    }
}

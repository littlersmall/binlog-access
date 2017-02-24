package com.littlersmall.biz.meta;

import com.littlersmall.model.BinlogMeta;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by littlersmall on 16/11/25.
 */
public class BinlogMetaBuilder {
    private JdbcTemplate jdbcTemplate;

    public BinlogMetaBuilder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BinlogMeta getBinlogMeta() {
        List<Map<String, Object>> binlogs = jdbcTemplate.queryForList("show binary logs");

        if (binlogs.size() > 0) {
            Map<String, Object> lastBinlog = binlogs.get(binlogs.size() - 1);
            BinlogMeta binlogMeta = new BinlogMeta();

            binlogMeta.setBinlogName((String) lastBinlog.get("Log_name"));
            binlogMeta.setPos(((BigInteger) lastBinlog.get("File_size")).longValue());

            return binlogMeta;
        } else {
            //no binlog
            return null;
        }
    }

    public static void main(String[] args) {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/lx_charge");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("root");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(basicDataSource);

        BinlogMetaBuilder binlogMetaBuilder = new BinlogMetaBuilder(jdbcTemplate);

        System.out.println(binlogMetaBuilder.getBinlogMeta());
    }
}

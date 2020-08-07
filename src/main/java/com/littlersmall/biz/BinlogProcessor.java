package com.littlersmall.biz;

import javax.annotation.PostConstruct;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.littlersmall.biz.event.AllEventProcessor;
import com.littlersmall.biz.meta.BinlogMetaBuilder;
import com.littlersmall.biz.meta.TableMetaCache;
import com.littlersmall.biz.sender.RowDiffMessageSender;

/**
 * Created by littlersmall on 16/11/30.
 */
@Service
public class BinlogProcessor {
    private RetryReplicator retryReplicator;

    @Value("${db.host}")
    private String host;

    @Value("${db.port}")
    private int port;

    @Qualifier("buildDataSource")
    @Autowired
    BasicDataSource basicDataSource;

    @Autowired
    RowDiffMessageSender messageSender;

    @PostConstruct
    public void init() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(basicDataSource);
        TableMetaCache tableMetaCache = new TableMetaCache(jdbcTemplate);
        AllEventProcessor allEventProcessor = new AllEventProcessor(tableMetaCache, messageSender);
        BinlogMetaBuilder binlogMetaBuilder = new BinlogMetaBuilder(jdbcTemplate);

        initReplicator(binlogMetaBuilder, allEventProcessor);
    }

    private void initReplicator(BinlogMetaBuilder binlogMetaBuilder, AllEventProcessor allEventProcessor) {
        retryReplicator = new RetryReplicator(binlogMetaBuilder);
        retryReplicator.setUser(basicDataSource.getUsername());
        retryReplicator.setPassword(basicDataSource.getPassword());
        retryReplicator.setHost(host);
        retryReplicator.setPort(port);
        //todo change serverId
        retryReplicator.setServerId((int) (System.currentTimeMillis() % 100000));
        retryReplicator.setBinlogEventListener(allEventProcessor::process);
    }

    public void start() throws Exception {
        retryReplicator.start();
    }

    public void stop() throws Exception {
        retryReplicator.stop();
    }
}

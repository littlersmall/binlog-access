package com.littlersmall.biz.sender;

import com.littlersmall.common.Constants;
import com.yidian.commerce.common.utils.common.DetailRes;
import com.yidian.commerce.common.utils.frame.Log;
import com.yidian.commerce.common.utils.model.RowDiffModel;
import com.yidian.commerce.common.utils.rabbitmq.MQAccessBuilder;
import com.yidian.commerce.common.utils.rabbitmq.MessageSender;
import com.yidian.commerce.common.utils.redis.RedisCache;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by littlersmall on 16/5/25.
 */
@Service
public class RowDiffMessageSender {
    private static final String EXCHANGE = "db-diff";
    private static final String ROUTING = "row-diff";

    private RedisCache redisCache;

    @Value("${diff.db}")
    private String diffDb;

    private Set<String> dbSet = new HashSet<>();

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    ConnectionFactory connectionFactory;

    private MessageSender messageSender;

    @PostConstruct
    public void init() throws IOException {
        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        messageSender = mqAccessBuilder.buildTopicMessageSender(EXCHANGE, ROUTING);
        redisCache = new RedisCache(redisTemplate);

        if (diffDb != null
                && !diffDb.isEmpty()) {
            Arrays.stream(diffDb.split(",")).forEach(db -> {
                dbSet.add(db.toLowerCase());
            });
        }
    }

    @Log
    public DetailRes send(long pos, List<RowDiffModel> rowDiffModels) {
        if (redisCache.cacheIfAbsent("binlog:" + pos, Constants.TIMESTAMP_VALID_TIME)) {
            DetailRes detailRes = new DetailRes(true, "");

            for (RowDiffModel rowDiffModel : rowDiffModels) {
                if (detailRes.isSuccess()) {
                    String dbName = rowDiffModel.getTableName().split("\\.")[0].toLowerCase();

                    if (dbSet.isEmpty()) {
                        detailRes = messageSender.send(rowDiffModel);
                    } else {
                        if (dbSet.contains(dbName)) {
                            detailRes = messageSender.send(rowDiffModel);
                        }
                    }
                } else {
                    break;
                }
            }

            return detailRes;
        } else {
            return new DetailRes(true, "");
        }
    }
}

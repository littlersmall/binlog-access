package com.littlersmall.biz.consumer;

import com.littlersmall.common.Constants;
import com.yidian.commerce.common.utils.common.DetailRes;
import com.yidian.commerce.common.utils.model.RowDiffModel;
import com.yidian.commerce.common.utils.rabbitmq.MQAccessBuilder;
import com.yidian.commerce.common.utils.rabbitmq.MessageProcess;
import com.yidian.commerce.common.utils.rabbitmq.ThreadPoolConsumer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by littlersmall on 16/12/5.
 */
@Service
public class RowDiffRawMessageConsumerPool {
        private static final String EXCHANGE = "db-diff";
        private static final String ROUTING = "row-diff";
        private static final String QUEUE = "row-diff-raw";

        @Autowired
        ConnectionFactory connectionFactory;

        private ThreadPoolConsumer<RowDiffModel> threadPoolConsumer;

        @PostConstruct
        public void init() {
            MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
            MessageProcess<RowDiffModel> messageProcess = message -> {
                System.out.println("received: " + message);

                return new DetailRes(true, "");
            };

            threadPoolConsumer = new ThreadPoolConsumer.ThreadPoolConsumerBuilder<RowDiffModel>()
                    .setThreadCount(Constants.CONSUMER_THREAD_COUNT).setIntervalMils(Constants.INTERVAL_MILS)
                    .setExchange(EXCHANGE).setRoutingKey(ROUTING).setQueue(QUEUE).setType("topic")
                    .setMQAccessBuilder(mqAccessBuilder).setMessageProcess(messageProcess)
                    .build();
        }

        public void start() throws IOException {
            threadPoolConsumer.start();
        }

        public void stop() {
            threadPoolConsumer.stop();
        }
}

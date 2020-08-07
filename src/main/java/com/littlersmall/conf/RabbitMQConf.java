package com.littlersmall.conf;


import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by littlersmall on 16/5/16.
 */
@Configuration
public class RabbitMQConf {
    @Value("${rabbit.ip}")
    private String ip;

    @Value("${rabbit.port}")
    private int port;

    @Value("${rabbit.user_name}")
    private String userName;

    @Value("${rabbit.password}")
    private String password;

    @Bean
    public ConnectionFactory binlogConnectionFactory() {
        System.out.println("ip = " + ip);
        System.out.println("port = " + port);

        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(ip, port);

        connectionFactory.setUsername(userName);
        connectionFactory.setPassword(password);
        connectionFactory.setPublisherConfirms(true); // enable confirm mode

        return connectionFactory;
    }
}

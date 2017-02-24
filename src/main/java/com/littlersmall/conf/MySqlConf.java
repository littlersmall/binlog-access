package com.littlersmall.conf;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by littlersmall on 16/5/18.
 */
@Configuration
public class MySqlConf {
    @Value("${db.url}")
    private String url;

    @Value("${db.username}")
    private String username;

    @Value("${db.password}")
    private String password;

    @Bean
    public BasicDataSource buildDataSource() {
        return buildBasicDataSource(url, username, password);
    }

    private BasicDataSource buildBasicDataSource(String url, String username, String password) {
        BasicDataSource basicDataSource = new BasicDataSource();

        basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        basicDataSource.setValidationQuery("SELECT 1");
        basicDataSource.setTestOnBorrow(false);
        basicDataSource.setTestOnReturn(false);
        basicDataSource.setTestWhileIdle(true);
        basicDataSource.setTimeBetweenEvictionRunsMillis(300000);
        basicDataSource.setMinEvictableIdleTimeMillis(1800000);
        basicDataSource.setNumTestsPerEvictionRun(-1);
        basicDataSource.setInitialSize(5);
        basicDataSource.setMaxActive(10);
        basicDataSource.setMaxIdle(5);
        basicDataSource.setMinIdle(5);
        basicDataSource.setMaxWait(100);

        return basicDataSource;
    }
}

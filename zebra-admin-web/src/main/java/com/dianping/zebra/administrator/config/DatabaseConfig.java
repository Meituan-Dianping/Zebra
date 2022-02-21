package com.dianping.zebra.administrator.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig configuration = new HikariConfig();
        configuration.setDriverClassName("com.mysql.cj.jdbc.Driver");
        configuration.setReadOnly(false);
        configuration.setConnectionTimeout(30000);
        configuration.setIdleTimeout(600000);
        configuration.setMaxLifetime(1800000);
        configuration.setMaximumPoolSize(30);
        configuration.setMinimumIdle(5);
        return new HikariDataSource(configuration);
    }

    @Bean
    public DataSourceTransactionManager zebraAdminTransactionManager(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }
}

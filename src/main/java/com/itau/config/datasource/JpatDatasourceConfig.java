package com.itau.config.datasource;

import com.itau.utils.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JpatDatasourceConfig {

    @Bean(name = Constants.BEAN_DATASOURCE_JPAT)
    @ConfigurationProperties(prefix = Constants.PREFIX_DATASOURCE_JPAT)
    @Primary
    public DataSource getJpatDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = Constants.BEAN_JDBC_TEMPLATE_JPAT)
    @Primary
    public JdbcTemplate getJpatJdbcTemplate(@Qualifier(Constants.BEAN_DATASOURCE_JPAT) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}

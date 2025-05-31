package co.com.itau.config.datasource;

import co.com.itau.utils.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class H2HDatasourceConfig {

    @Bean(name = Constants.BEAN_DATA_SOURCE)
    @BatchDataSource
    @ConfigurationProperties(prefix = Constants.PREFIX_DATASOURCE_H2)
    public DataSource getH2Datasource(){
        return DataSourceBuilder.create().build();
    }

    @Bean(name = Constants.BEAN_JDBC_TEMPLATE_H2)
    public JdbcTemplate getH2JdbcTemplate(@Qualifier(Constants.BEAN_DATA_SOURCE) DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
}

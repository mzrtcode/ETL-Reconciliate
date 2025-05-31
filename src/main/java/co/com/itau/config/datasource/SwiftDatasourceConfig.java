package co.com.itau.config.datasource;

import co.com.itau.utils.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SwiftDatasourceConfig {

    @Bean(name = Constants.BEAN_DATASOURCE_SWIFT)
    @ConfigurationProperties(prefix = Constants.PREFIX_DATASOURCE_SWIFT)
    public DataSource getDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = Constants.BEAN_JDBC_TEMPLATE_SWIFT)
    public JdbcTemplate getJdbcTemplate(@Qualifier(Constants.BEAN_DATASOURCE_SWIFT) DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}

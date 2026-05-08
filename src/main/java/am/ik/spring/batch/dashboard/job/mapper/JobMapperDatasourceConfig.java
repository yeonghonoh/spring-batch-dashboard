package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.mapper.mysql.MysqlJobExecutionMapper;
import am.ik.spring.batch.dashboard.job.mapper.mysql.MysqlJobInstanceMapper;
import am.ik.spring.batch.dashboard.job.mapper.mysql.MysqlJobStatisticsMapper;
import am.ik.spring.batch.dashboard.job.mapper.mysql.MysqlStepExecutionMapper;
import am.ik.spring.batch.dashboard.job.mapper.postgres.PostgresJobExecutionMapper;
import am.ik.spring.batch.dashboard.job.mapper.postgres.PostgresJobInstanceMapper;
import am.ik.spring.batch.dashboard.job.mapper.postgres.PostgresJobStatisticsMapper;
import am.ik.spring.batch.dashboard.job.mapper.postgres.PostgresStepExecutionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration(proxyBeanMethods = false)
public class JobMapperDatasourceConfig implements InitializingBean {

	private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

	private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";

	private static final Set<String> SUPPORTED_DRIVERS = Set.of(POSTGRES_DRIVER, MYSQL_DRIVER);

	private final String driverClassName;

	public JobMapperDatasourceConfig(DataSourceProperties dataSourceProperties) {
		this.driverClassName = dataSourceProperties.determineDriverClassName();
	}

	@Override
	public void afterPropertiesSet() {
		if (driverClassName == null || !SUPPORTED_DRIVERS.contains(driverClassName)) {
			throw new IllegalStateException("Unsupported spring.datasource.driver-class-name: " + driverClassName
					+ " (supported: " + String.join(", ", SUPPORTED_DRIVERS) + ")");
		}
	}

	@Bean
	public JobInstanceMapper jobInstanceMapper(JdbcClient jdbcClient) {
		return switch (driverClassName) {
			case POSTGRES_DRIVER -> new PostgresJobInstanceMapper(jdbcClient);
			case MYSQL_DRIVER -> new MysqlJobInstanceMapper(jdbcClient);
			default -> throw unsupported();
		};
	}

	@Bean
	public JobExecutionMapper jobExecutionMapper(JdbcClient jdbcClient) {
		return switch (driverClassName) {
			case POSTGRES_DRIVER -> new PostgresJobExecutionMapper(jdbcClient);
			case MYSQL_DRIVER -> new MysqlJobExecutionMapper(jdbcClient);
			default -> throw unsupported();
		};
	}

	@Bean
	public StepExecutionMapper stepExecutionMapper(JdbcClient jdbcClient) {
		return switch (driverClassName) {
			case POSTGRES_DRIVER -> new PostgresStepExecutionMapper(jdbcClient);
			case MYSQL_DRIVER -> new MysqlStepExecutionMapper(jdbcClient);
			default -> throw unsupported();
		};
	}

	@Bean
	public JobStatisticsMapper jobStatisticsMapper(JdbcClient jdbcClient, ObjectMapper objectMapper) {
		return switch (driverClassName) {
			case POSTGRES_DRIVER -> new PostgresJobStatisticsMapper(jdbcClient, objectMapper);
			case MYSQL_DRIVER -> new MysqlJobStatisticsMapper(jdbcClient, objectMapper);
			default -> throw unsupported();
		};
	}

	private IllegalStateException unsupported() {
		return new IllegalStateException(
				"Unreachable: driver " + driverClassName + " passed validation but no case matched");
	}

}

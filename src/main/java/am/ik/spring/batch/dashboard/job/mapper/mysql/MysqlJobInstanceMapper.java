package am.ik.spring.batch.dashboard.job.mapper.mysql;

import am.ik.spring.batch.dashboard.job.JobExecution;
import am.ik.spring.batch.dashboard.job.JobExecutionSummaryBuilder;
import am.ik.spring.batch.dashboard.job.JobInstance;
import am.ik.spring.batch.dashboard.job.JobInstanceBuilder;
import am.ik.spring.batch.dashboard.job.JobInstanceDetail;
import am.ik.spring.batch.dashboard.job.JobInstanceDetailBuilder;
import am.ik.spring.batch.dashboard.job.JobInstancesParams;
import am.ik.spring.batch.dashboard.job.JobStatus;
import am.ik.spring.batch.dashboard.job.PageResponse;
import am.ik.spring.batch.dashboard.job.PageResponseBuilder;
import am.ik.spring.batch.dashboard.job.mapper.JobInstanceMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "spring.datasource.driver-class-name", havingValue = "com.mysql.cj.jdbc.Driver")
public class MysqlJobInstanceMapper implements JobInstanceMapper {

	private final JdbcClient jdbcClient;

	public MysqlJobInstanceMapper(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	public PageResponse<JobInstance> findJobInstances(JobInstancesParams params) {
		Integer page = Objects.requireNonNullElse(params.page(), 0);
		Integer size = Objects.requireNonNullElse(params.size(), 20);
		List<JobInstance> content = this.jdbcClient.sql("""
				SELECT
				    ji.JOB_INSTANCE_ID,
				    ji.JOB_NAME,
				    ji.JOB_KEY,
				    ji.VERSION,
				    je.JOB_EXECUTION_ID,
				    je.START_TIME,
				    je.END_TIME,
				    je.STATUS
				FROM
				    BATCH_JOB_INSTANCE ji
				    LEFT JOIN
				        (
				            SELECT
				                je1.*
				            FROM
				                BATCH_JOB_EXECUTION je1
				                JOIN
				                    (
				                        SELECT
				                            JOB_INSTANCE_ID,
				                            MAX(JOB_EXECUTION_ID) as MAX_EXECUTION_ID
				                        FROM
				                            BATCH_JOB_EXECUTION
				                        GROUP BY
				                            JOB_INSTANCE_ID
				                    ) je2
				                ON  je1.JOB_EXECUTION_ID = je2.MAX_EXECUTION_ID
				        ) je
				    ON  ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
				WHERE
				    (:jobName IS NULL OR ji.JOB_NAME = :jobName)
				ORDER BY
				    ji.JOB_INSTANCE_ID DESC
				LIMIT :size OFFSET :offset
				""")
			.param("jobName", params.jobName())
			.param("offset", page * size)
			.param("size", size)
			.query((rs, rowNum) -> JobInstanceBuilder.jobInstance()
				.jobInstanceId(rs.getLong("JOB_INSTANCE_ID"))
				.jobName(rs.getString("JOB_NAME"))
				.jobKey(rs.getString("JOB_KEY"))
				.version(rs.getInt("VERSION"))
				.latestExecution(JobExecutionSummaryBuilder.jobExecutionSummary()
					.jobExecutionId(rs.getLong("JOB_EXECUTION_ID"))
					.startTime(rs.getObject("START_TIME", LocalDateTime.class))
					.endTime(rs.getObject("END_TIME", LocalDateTime.class))
					.status(JobStatus.valueOf(rs.getString("STATUS")))
					.build())
				.build())
			.list();

		long count = this.jdbcClient.sql("""
				SELECT COUNT(*)
				FROM BATCH_JOB_INSTANCE ji
				WHERE (:jobName IS NULL OR ji.JOB_NAME = :jobName)
				""").param("jobName", params.jobName()).query(Long.class).single();

		return PageResponseBuilder.<JobInstance>pageResponse()
			.content(content)
			.page(page)
			.size(size)
			.totalElements(count)
			.totalPages((int) (count / size) + 1)
			.build();
	}

	@Override
	public Optional<JobInstanceDetail> getJobInstanceDetail(long jobInstanceId) {
		return this.jdbcClient.sql("""
				SELECT
				    ji.JOB_INSTANCE_ID,
				    ji.JOB_NAME,
				    ji.JOB_KEY,
				    ji.VERSION,
				    je.JOB_EXECUTION_ID,
				    je.CREATE_TIME,
				    je.START_TIME,
				    je.END_TIME,
				    je.STATUS,
				    je.EXIT_CODE,
				    je.EXIT_MESSAGE,
				    je.LAST_UPDATED
				FROM
				    BATCH_JOB_INSTANCE ji
				    JOIN
				        BATCH_JOB_EXECUTION je
				    ON  ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
				WHERE
				    ji.JOB_INSTANCE_ID = :jobInstanceId
				""")
			.param("jobInstanceId", jobInstanceId)
			.query((rs, rowNum) -> JobInstanceDetailBuilder.jobInstanceDetail()
				.jobInstanceId(rs.getLong("JOB_INSTANCE_ID"))
				.jobName(rs.getString("JOB_NAME"))
				.jobKey(rs.getString("JOB_KEY"))
				.version(rs.getInt("VERSION"))
				.latestExecution(JobExecutionSummaryBuilder.jobExecutionSummary()
					.jobExecutionId(rs.getLong("JOB_EXECUTION_ID"))
					.startTime(rs.getObject("START_TIME", LocalDateTime.class))
					.endTime(rs.getObject("END_TIME", LocalDateTime.class))
					.status(JobStatus.valueOf(rs.getString("STATUS")))
					.build())
				.executions(List.of())
				.build())
			.optional()
			.map(jobInstanceDetail -> JobInstanceDetailBuilder.from(jobInstanceDetail)
				.executions(this.jdbcClient.sql("""
						SELECT
						    je.JOB_EXECUTION_ID,
						    je.JOB_INSTANCE_ID,
						    ji.JOB_NAME,
						    je.CREATE_TIME,
						    je.START_TIME,
						    je.END_TIME,
						    je.STATUS,
						    je.EXIT_CODE,
						    je.EXIT_MESSAGE
						FROM
						    BATCH_JOB_EXECUTION je
						    JOIN
						        BATCH_JOB_INSTANCE ji
						    ON  je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
						WHERE
						    je.JOB_INSTANCE_ID = :jobInstanceId
						ORDER BY
						    je.START_TIME DESC
						""").param("jobInstanceId", jobInstanceId).query(JobExecution.class).list())
				.build());
	}

}

package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.JobExecution;
import am.ik.spring.batch.dashboard.job.JobExecutionDetail;
import am.ik.spring.batch.dashboard.job.JobExecutionsParams;
import am.ik.spring.batch.dashboard.job.PageResponse;
import java.util.Optional;

public interface JobExecutionMapper {

	PageResponse<JobExecution> findJobExecutions(JobExecutionsParams params);

	Optional<JobExecutionDetail> getJobExecutionDetail(long jobExecutionId);

}
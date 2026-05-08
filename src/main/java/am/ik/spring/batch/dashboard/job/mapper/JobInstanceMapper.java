package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.JobInstance;
import am.ik.spring.batch.dashboard.job.JobInstanceDetail;
import am.ik.spring.batch.dashboard.job.JobInstancesParams;
import am.ik.spring.batch.dashboard.job.PageResponse;
import java.util.Optional;

public interface JobInstanceMapper {

	PageResponse<JobInstance> findJobInstances(JobInstancesParams params);

	Optional<JobInstanceDetail> getJobInstanceDetail(long jobInstanceId);

}
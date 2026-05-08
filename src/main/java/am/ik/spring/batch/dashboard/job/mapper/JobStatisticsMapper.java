package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.JobExecutionStats;
import am.ik.spring.batch.dashboard.job.JobSpecificStatistics;
import am.ik.spring.batch.dashboard.job.JobStatistics;
import java.util.List;
import java.util.Optional;

public interface JobStatisticsMapper {

	JobStatistics getJobStatistics(int days);

	Optional<JobSpecificStatistics> getJobStatisticsByJobName(String jobName);

	List<JobExecutionStats> getJobExecutionStats(int days);

}
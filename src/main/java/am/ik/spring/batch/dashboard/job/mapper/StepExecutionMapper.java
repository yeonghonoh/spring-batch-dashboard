package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.StepExecutionDetail;
import java.util.Optional;

public interface StepExecutionMapper {

	Optional<StepExecutionDetail> getStepExecutionDetail(long stepExecutionId);

}
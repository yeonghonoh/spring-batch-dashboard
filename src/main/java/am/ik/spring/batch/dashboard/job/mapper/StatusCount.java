package am.ik.spring.batch.dashboard.job.mapper;

import am.ik.spring.batch.dashboard.job.JobStatus;

public record StatusCount(JobStatus status, Long count) {
}
package com.jobpilot.application.resume.usecase;

import com.jobpilot.application.resume.dto.ListResumesCommand;
import com.jobpilot.application.resume.dto.ResumeResponse;
import com.jobpilot.application.shared.UseCase;

import java.util.List;

public interface ListResumesUseCase extends UseCase<ListResumesCommand, List<ResumeResponse>> {
}

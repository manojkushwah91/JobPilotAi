package com.jobpilot.application.coverletter.usecase;

import com.jobpilot.application.coverletter.dto.CoverLetterResponse;
import com.jobpilot.application.coverletter.dto.CreateCoverLetterCommand;
import com.jobpilot.application.shared.UseCase;

public interface CreateCoverLetterUseCase extends UseCase<CreateCoverLetterCommand, CoverLetterResponse> {
}

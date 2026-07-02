package com.jobpilot.application.coverletter.usecase;

import com.jobpilot.application.coverletter.dto.CoverLetterResponse;
import com.jobpilot.application.coverletter.dto.GenerateCoverLetterCommand;
import com.jobpilot.application.shared.UseCase;

public interface GenerateCoverLetterUseCase extends UseCase<GenerateCoverLetterCommand, CoverLetterResponse> {
}

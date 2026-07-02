package com.jobpilot.application.questionbank.usecase;

import com.jobpilot.application.questionbank.dto.AddQuestionCommand;
import com.jobpilot.application.questionbank.dto.QuestionBankResponse;
import com.jobpilot.application.shared.UseCase;

public interface AddQuestionUseCase extends UseCase<AddQuestionCommand, QuestionBankResponse> {}

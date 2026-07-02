package com.jobpilot.application.questionbank.usecase;

import com.jobpilot.application.questionbank.dto.QuestionBankResponse;
import com.jobpilot.application.questionbank.dto.SearchQuestionsCommand;
import com.jobpilot.application.shared.UseCase;
import org.springframework.data.domain.Page;

public interface SearchQuestionsUseCase extends UseCase<SearchQuestionsCommand, Page<QuestionBankResponse>> {}

package com.jobpilot.application.questionbank.service;

import com.jobpilot.application.questionbank.dto.QuestionBankResponse;
import com.jobpilot.application.questionbank.dto.SearchQuestionsCommand;
import com.jobpilot.application.questionbank.ports.QuestionBankRepository;
import com.jobpilot.application.questionbank.usecase.SearchQuestionsUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchQuestionsService implements SearchQuestionsUseCase {

    private final QuestionBankRepository questionBankRepository;

    public SearchQuestionsService(QuestionBankRepository questionBankRepository) {
        this.questionBankRepository = questionBankRepository;
    }

    @Override
    public Page<QuestionBankResponse> execute(SearchQuestionsCommand command) {
        var pageable = PageRequest.of(command.page(), command.size());
        return questionBankRepository.search(command.query(), command.category(), command.type(), command.difficulty(), pageable)
            .map(QuestionBankResponse::from);
    }
}

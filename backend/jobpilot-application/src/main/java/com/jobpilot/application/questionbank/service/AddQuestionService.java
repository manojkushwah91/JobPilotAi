package com.jobpilot.application.questionbank.service;

import com.jobpilot.application.questionbank.dto.AddQuestionCommand;
import com.jobpilot.application.questionbank.dto.QuestionBankResponse;
import com.jobpilot.application.questionbank.ports.QuestionBankRepository;
import com.jobpilot.application.questionbank.usecase.AddQuestionUseCase;
import com.jobpilot.domain.questionbank.QuestionBankEntry;
import com.jobpilot.domain.questionbank.QuestionBankId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddQuestionService implements AddQuestionUseCase {

    private final QuestionBankRepository questionBankRepository;

    public AddQuestionService(QuestionBankRepository questionBankRepository) {
        this.questionBankRepository = questionBankRepository;
    }

    @Override
    public QuestionBankResponse execute(AddQuestionCommand command) {
        var id = QuestionBankId.generate();
        var entry = QuestionBankEntry.create(id, command.type(), command.category(), command.question(), command.difficulty());
        questionBankRepository.save(entry);
        return QuestionBankResponse.from(entry);
    }
}

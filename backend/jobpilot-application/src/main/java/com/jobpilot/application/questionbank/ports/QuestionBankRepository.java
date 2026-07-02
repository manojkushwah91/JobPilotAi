package com.jobpilot.application.questionbank.ports;

import com.jobpilot.domain.questionbank.QuestionBankEntry;
import com.jobpilot.domain.questionbank.QuestionBankId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface QuestionBankRepository {
    Page<QuestionBankEntry> search(String query, String category, String type, Integer difficulty, Pageable pageable);
    Optional<QuestionBankEntry> findById(QuestionBankId id);
    QuestionBankEntry save(QuestionBankEntry entry);
}

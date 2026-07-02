package com.jobpilot.interfaces.rest.v1.questionbank;

import com.jobpilot.application.questionbank.dto.AddQuestionCommand;
import com.jobpilot.application.questionbank.dto.QuestionBankResponse;
import com.jobpilot.application.questionbank.dto.SearchQuestionsCommand;
import com.jobpilot.application.questionbank.usecase.AddQuestionUseCase;
import com.jobpilot.application.questionbank.usecase.SearchQuestionsUseCase;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/question-bank")
public class QuestionBankController {

    private final AddQuestionUseCase addQuestionUseCase;
    private final SearchQuestionsUseCase searchQuestionsUseCase;

    public QuestionBankController(AddQuestionUseCase addQuestionUseCase,
                                   SearchQuestionsUseCase searchQuestionsUseCase) {
        this.addQuestionUseCase = addQuestionUseCase;
        this.searchQuestionsUseCase = searchQuestionsUseCase;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionBankResponse>> create(@RequestBody AddQuestionCommand command) {
        var response = addQuestionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<QuestionBankResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @PageableDefault Pageable pageable) {
        var command = new SearchQuestionsCommand(query, category, difficulty, type, pageable.getPageNumber(), pageable.getPageSize());
        var response = searchQuestionsUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}

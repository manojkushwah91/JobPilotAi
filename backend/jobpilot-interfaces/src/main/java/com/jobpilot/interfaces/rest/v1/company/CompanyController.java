package com.jobpilot.interfaces.rest.v1.company;

import com.jobpilot.application.company.dto.*;
import com.jobpilot.application.company.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final GetCompanyUseCase getCompanyUseCase;

    public CompanyController(CreateCompanyUseCase createCompanyUseCase,
                              UpdateCompanyUseCase updateCompanyUseCase,
                              GetCompanyUseCase getCompanyUseCase) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
    }

    @RateLimited(capacity = 100)
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CreateCompanyRequest request) {
        var command = new CreateCompanyCommand(request.name(), request.description(), request.website(),
            request.logoUrl(), request.industry(), request.headquarters(), request.foundedYear(),
            request.companySizeMin(), request.companySizeMax(), request.stockSymbol(),
            request.technologyStack(), request.cultureKeywords());
        var response = createCompanyUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getById(@PathVariable String id) {
        var response = getCompanyUseCase.execute(new GetCompanyCommand(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @RateLimited(capacity = 100)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(@PathVariable String id,
            @Valid @RequestBody CreateCompanyRequest request) {
        var command = new UpdateCompanyCommand(id, request.name(), request.description(), request.website(),
            request.logoUrl(), request.industry(), request.headquarters(), request.foundedYear(),
            request.companySizeMin(), request.companySizeMax(), request.stockSymbol(),
            request.technologyStack(), request.cultureKeywords());
        var response = updateCompanyUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    public record CreateCompanyRequest(
        @NotBlank @Size(max = 255) String name, String description, String website, String logoUrl,
        String industry, Map<String, Object> headquarters, Integer foundedYear,
        Integer companySizeMin, Integer companySizeMax, String stockSymbol,
        List<String> technologyStack, List<String> cultureKeywords
    ) {}
}

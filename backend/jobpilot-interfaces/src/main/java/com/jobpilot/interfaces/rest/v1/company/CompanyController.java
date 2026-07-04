package com.jobpilot.interfaces.rest.v1.company;

import com.jobpilot.application.company.dto.*;
import com.jobpilot.application.company.ports.CompanyRepository;
import com.jobpilot.application.company.usecase.*;
import com.jobpilot.common.model.ApiResponse;
import com.jobpilot.domain.company.CompanyId;
import com.jobpilot.interfaces.rest.annotation.RateLimited;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final GetCompanyUseCase getCompanyUseCase;
    private final CompanyRepository companyRepository;

    public CompanyController(CreateCompanyUseCase createCompanyUseCase,
                              UpdateCompanyUseCase updateCompanyUseCase,
                              GetCompanyUseCase getCompanyUseCase,
                              CompanyRepository companyRepository) {
        this.createCompanyUseCase = createCompanyUseCase;
        this.updateCompanyUseCase = updateCompanyUseCase;
        this.getCompanyUseCase = getCompanyUseCase;
        this.companyRepository = companyRepository;
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

    @RateLimited(capacity = 100)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> search(
            @RequestParam(name = "query") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = companyRepository.search(query, PageRequest.of(page, size))
            .map(CompanyResponse::from);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/tech-stack")
    public ResponseEntity<ApiResponse<List<String>>> techStack(@PathVariable String id) {
        var company = companyRepository.findById(CompanyId.from(UUID.fromString(id)));
        return ResponseEntity.ok(ApiResponse.ok(
            company.map(c -> c.technologyStack() != null ? c.technologyStack() : List.<String>of())
                .orElse(List.of())
        ));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/salary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> salary(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "minSalary", 0, "maxSalary", 0, "currency", "USD",
            "averageSalary", 0, "sampleSize", 0
        )));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/interviews")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> interviews(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(List.of()));
    }

    @RateLimited(capacity = 100)
    @GetMapping("/{id}/hiring-trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hiringTrends(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
            "trend", "stable",
            "last6Months", List.of(),
            "growthRate", 0.0
        )));
    }

    public record CreateCompanyRequest(
        @NotBlank @Size(max = 255) String name, String description, String website, String logoUrl,
        String industry, Map<String, Object> headquarters, Integer foundedYear,
        Integer companySizeMin, Integer companySizeMax, String stockSymbol,
        List<String> technologyStack, List<String> cultureKeywords
    ) {}
}

package com.jobpilot.infrastructure.persistence.company;

import com.jobpilot.domain.company.CompanyId;
import com.jobpilot.domain.company.CompanyProfile;
import com.jobpilot.infrastructure.persistence.shared.BaseJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "company_profiles")
public class CompanyProfileEntity extends BaseJpaEntity {

    @Id private UUID id;
    @Column(name = "name", nullable = false, unique = true) private String name;
    @Column(name = "description", columnDefinition = "text") private String description;
    @Column(name = "website") private String website;
    @Column(name = "logo_url") private String logoUrl;
    @Column(name = "industry") private String industry;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headquarters", columnDefinition = "jsonb") private String headquarters;
    @Column(name = "founded_year") private Integer foundedYear;
    @Column(name = "company_size_min") private Integer companySizeMin;
    @Column(name = "company_size_max") private Integer companySizeMax;
    @Column(name = "stock_symbol") private String stockSymbol;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "funding_rounds", columnDefinition = "jsonb") private String fundingRounds;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technology_stack", columnDefinition = "jsonb") private String technologyStack;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "culture_keywords", columnDefinition = "jsonb") private String cultureKeywords;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "salary_data", columnDefinition = "jsonb") private String salaryData;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "hiring_trends", columnDefinition = "jsonb") private String hiringTrends;

    protected CompanyProfileEntity() {}

    public static CompanyProfileEntity fromDomain(CompanyProfile c) {
        var e = new CompanyProfileEntity();
        e.id = c.companyId().value();
        e.name = c.name();
        e.description = c.description();
        e.website = c.website();
        e.logoUrl = c.logoUrl();
        e.industry = c.industry();
        e.headquarters = toJson(c.headquarters());
        e.foundedYear = c.foundedYear();
        e.companySizeMin = c.companySizeMin();
        e.companySizeMax = c.companySizeMax();
        e.stockSymbol = c.stockSymbol();
        e.fundingRounds = toJson(c.fundingRounds());
        e.technologyStack = toJson(c.technologyStack());
        e.cultureKeywords = toJson(c.cultureKeywords());
        e.salaryData = toJson(c.salaryData());
        e.hiringTrends = toJson(c.hiringTrends());
        return e;
    }

    @SuppressWarnings("unchecked")
    public CompanyProfile toDomain() {
        return CompanyProfile.reconstitute(CompanyId.from(id), name, description, website, logoUrl,
            industry, fromJson(headquarters), foundedYear, companySizeMin, companySizeMax,
            stockSymbol, fromJsonListMap(fundingRounds), fromJsonList(technologyStack),
            fromJsonList(cultureKeywords), fromJson(salaryData), fromJson(hiringTrends),
            createdAt, updatedAt);
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();
    private static String toJson(Object obj) {
        try { return obj != null ? MAPPER.writeValueAsString(obj) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static Map<String, Object> fromJson(String json) {
        try { return json != null ? MAPPER.readValue(json, Map.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static List<String> fromJsonList(String json) {
        try { return json != null ? MAPPER.readValue(json, List.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> fromJsonListMap(String json) {
        try { return json != null ? MAPPER.readValue(json, List.class) : null; }
        catch (Exception e) { throw new RuntimeException("JSON error", e); }
    }
}

package com.jobpilot.domain.company;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CompanyProfile extends BaseAggregateRoot {

    private CompanyId companyId;
    private String name;
    private String description;
    private String website;
    private String logoUrl;
    private String industry;
    private Map<String, Object> headquarters;
    private Integer foundedYear;
    private Integer companySizeMin;
    private Integer companySizeMax;
    private String stockSymbol;
    private List<Map<String, Object>> fundingRounds;
    private List<String> technologyStack;
    private List<String> cultureKeywords;
    private Map<String, Object> salaryData;
    private Map<String, Object> hiringTrends;
    private final Instant createdAt;
    private Instant updatedAt;

    private CompanyProfile(CompanyId companyId, String name) {
        super(companyId.value());
        this.companyId = companyId;
        this.name = name;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static CompanyProfile create(CompanyId companyId, String name) {
        return new CompanyProfile(companyId, name);
    }

    public static CompanyProfile reconstitute(CompanyId companyId, String name, String description,
            String website, String logoUrl, String industry, Map<String, Object> headquarters,
            Integer foundedYear, Integer companySizeMin, Integer companySizeMax, String stockSymbol,
            List<Map<String, Object>> fundingRounds, List<String> technologyStack,
            List<String> cultureKeywords, Map<String, Object> salaryData,
            Map<String, Object> hiringTrends, Instant createdAt, Instant updatedAt) {
        var cp = new CompanyProfile(companyId, name);
        cp.description = description;
        cp.website = website;
        cp.logoUrl = logoUrl;
        cp.industry = industry;
        cp.headquarters = headquarters;
        cp.foundedYear = foundedYear;
        cp.companySizeMin = companySizeMin;
        cp.companySizeMax = companySizeMax;
        cp.stockSymbol = stockSymbol;
        cp.fundingRounds = fundingRounds;
        cp.technologyStack = technologyStack;
        cp.cultureKeywords = cultureKeywords;
        cp.salaryData = salaryData;
        cp.hiringTrends = hiringTrends;
        return cp;
    }

    public void updateDetails(String description, String website, String logoUrl, String industry,
            Map<String, Object> headquarters, Integer foundedYear, Integer companySizeMin,
            Integer companySizeMax, String stockSymbol, List<String> technologyStack,
            List<String> cultureKeywords) {
        this.description = description;
        this.website = website;
        this.logoUrl = logoUrl;
        this.industry = industry;
        this.headquarters = headquarters;
        this.foundedYear = foundedYear;
        this.companySizeMin = companySizeMin;
        this.companySizeMax = companySizeMax;
        this.stockSymbol = stockSymbol;
        this.technologyStack = technologyStack;
        this.cultureKeywords = cultureKeywords;
        this.updatedAt = Instant.now();
    }

    public CompanyId companyId() { return companyId; }
    public String name() { return name; }
    public String description() { return description; }
    public String website() { return website; }
    public String logoUrl() { return logoUrl; }
    public String industry() { return industry; }
    public Map<String, Object> headquarters() { return headquarters; }
    public Integer foundedYear() { return foundedYear; }
    public Integer companySizeMin() { return companySizeMin; }
    public Integer companySizeMax() { return companySizeMax; }
    public String stockSymbol() { return stockSymbol; }
    public List<Map<String, Object>> fundingRounds() { return fundingRounds; }
    public List<String> technologyStack() { return technologyStack; }
    public List<String> cultureKeywords() { return cultureKeywords; }
    public Map<String, Object> salaryData() { return salaryData; }
    public Map<String, Object> hiringTrends() { return hiringTrends; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}

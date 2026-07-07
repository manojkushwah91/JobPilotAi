package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.domain.agent.Tool;
import com.jobpilot.domain.job.JobId;
import com.jobpilot.domain.job.JobListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentToolsTest {

    @Mock
    private AiProviderPort aiProvider;

    @Mock
    private CandidateProfileRepository profileRepository;

    @Mock
    private JobRepository jobRepository;

    private JobDiscoveryTool jobDiscoveryTool;
    private ResumeTailoringTool resumeTailoringTool;
    private CoverLetterTool coverLetterTool;
    private JobScoringTool jobScoringTool;

    @BeforeEach
    void setUp() {
        jobDiscoveryTool = new JobDiscoveryTool(jobRepository);
        resumeTailoringTool = new ResumeTailoringTool(aiProvider, profileRepository);
        coverLetterTool = new CoverLetterTool(aiProvider, profileRepository);
        jobScoringTool = new JobScoringTool(aiProvider, profileRepository);
    }

    @Test
    void jobDiscoveryTool_shouldHaveCorrectName() {
        assertEquals("DISCOVER_JOBS", jobDiscoveryTool.name());
    }

    @Test
    void jobDiscoveryTool_shouldHaveDescription() {
        assertNotNull(jobDiscoveryTool.description());
        assertTrue(jobDiscoveryTool.description().contains("Searches"));
    }

    @Test
    void jobDiscoveryTool_shouldNotRequireApproval() {
        assertFalse(jobDiscoveryTool.requiresApproval());
    }

    @Test
    void jobDiscoveryTool_shouldExecuteWithDB() {
        var job = JobListing.create(JobId.generate(), "playwright-scraper", "Java Developer", "Google", "desc");
        when(jobRepository.search(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of(job)));

        var result = jobDiscoveryTool.execute(Map.of(
            "query", "Java Developer",
            "location", "Remote",
            "skills", "Java, Spring Boot"
        ));

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        verify(jobRepository).search(anyString(), any(PageRequest.class));
    }

    @Test
    void jobDiscoveryTool_shouldReturnEmptyWhenNoJobs() {
        when(jobRepository.search(anyString(), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of()));
        when(jobRepository.findAllActive(any(PageRequest.class)))
            .thenReturn(new PageImpl<>(List.of()));

        var result = jobDiscoveryTool.execute(Map.of(
            "query", "Nonexistent",
            "location", "Mars"
        ));

        assertNotNull(result);
        assertEquals("success", result.get("status"));
    }

    @Test
    void resumeTailoringTool_shouldHaveCorrectName() {
        assertEquals("TAILOR_RESUME", resumeTailoringTool.name());
    }

    @Test
    void resumeTailoringTool_shouldHaveDescription() {
        assertNotNull(resumeTailoringTool.description());
        assertTrue(resumeTailoringTool.description().contains("Tailors"));
    }

    @Test
    void resumeTailoringTool_shouldNotRequireApproval() {
        assertFalse(resumeTailoringTool.requiresApproval());
    }

    @Test
    void coverLetterTool_shouldHaveCorrectName() {
        assertEquals("GENERATE_COVER_LETTER", coverLetterTool.name());
    }

    @Test
    void coverLetterTool_shouldHaveDescription() {
        assertNotNull(coverLetterTool.description());
        assertTrue(coverLetterTool.description().contains("Generates"));
    }

    @Test
    void coverLetterTool_shouldNotRequireApproval() {
        assertFalse(coverLetterTool.requiresApproval());
    }

    @Test
    void jobScoringTool_shouldHaveCorrectName() {
        assertEquals("RANK_JOB", jobScoringTool.name());
    }

    @Test
    void jobScoringTool_shouldHaveDescription() {
        assertNotNull(jobScoringTool.description());
        assertTrue(jobScoringTool.description().contains("Scores"));
    }

    @Test
    void jobScoringTool_shouldNotRequireApproval() {
        assertFalse(jobScoringTool.requiresApproval());
    }

    @Test
    void resumeTailoringTool_shouldReturnErrorWithoutDescription() {
        var result = resumeTailoringTool.execute(Map.of("title", "Dev", "company", "Acme"));
        assertEquals("error", result.get("status"));
    }

    @Test
    void coverLetterTool_shouldReturnErrorWithoutDescription() {
        var result = coverLetterTool.execute(Map.of("title", "Dev", "company", "Acme"));
        assertEquals("error", result.get("status"));
    }

    @Test
    void jobScoringTool_shouldReturnErrorWithoutDescription() {
        var result = jobScoringTool.execute(Map.of("title", "Dev", "company", "Acme"));
        assertEquals("error", result.get("status"));
    }

    @Test
    void jobDiscoveryTool_shouldHaveTimeout() {
        assertEquals(30, jobDiscoveryTool.timeoutSeconds());
    }

    @Test
    void resumeTailoringTool_shouldHaveTimeout() {
        assertEquals(120, resumeTailoringTool.timeoutSeconds());
    }

    @Test
    void coverLetterTool_shouldHaveTimeout() {
        assertEquals(90, coverLetterTool.timeoutSeconds());
    }

    @Test
    void jobScoringTool_shouldHaveTimeout() {
        assertEquals(60, jobScoringTool.timeoutSeconds());
    }
}

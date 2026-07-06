package com.jobpilot.application.agent.tools;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.job.ports.JobRepository;
import com.jobpilot.application.resume.ports.ResumeVersionRepository;
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
    private ResumeVersionRepository resumeVersionRepository;

    @Mock
    private JobRepository jobRepository;

    private JobDiscoveryTool jobDiscoveryTool;
    private ResumeTailoringTool resumeTailoringTool;
    private CoverLetterGeneratorTool coverLetterGeneratorTool;

    @BeforeEach
    void setUp() {
        jobDiscoveryTool = new JobDiscoveryTool(jobRepository);
        resumeTailoringTool = new ResumeTailoringTool(aiProvider, resumeVersionRepository);
        coverLetterGeneratorTool = new CoverLetterGeneratorTool(aiProvider);
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
    void resumeTailoringTool_shouldExecuteWithAI() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("{\"tailoredResume\":\"...\",\"changes\":[\"Added keywords\"],\"atsScoreImprovement\":15}");

        var result = resumeTailoringTool.execute(Map.of(
            "resumeContent", "John Doe - Java Developer",
            "jobDescription", "Looking for Java developer with Spring Boot experience",
            "companyName", "Google"
        ));

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("Google", result.get("companyName"));
    }

    @Test
    void coverLetterGeneratorTool_shouldHaveCorrectName() {
        assertEquals("GENERATE_COVER_LETTER", coverLetterGeneratorTool.name());
    }

    @Test
    void coverLetterGeneratorTool_shouldHaveDescription() {
        assertNotNull(coverLetterGeneratorTool.description());
        assertTrue(coverLetterGeneratorTool.description().contains("Generates"));
    }

    @Test
    void coverLetterGeneratorTool_shouldNotRequireApproval() {
        assertFalse(coverLetterGeneratorTool.requiresApproval());
    }

    @Test
    void coverLetterGeneratorTool_shouldExecuteWithAI() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("{\"coverLetter\":\"Dear Hiring Manager...\",\"wordCount\":250,\"keyPoints\":[\"Java expert\"]}");

        var result = coverLetterGeneratorTool.execute(Map.of(
            "candidateProfile", "John Doe - 5 years Java experience",
            "jobDescription", "Senior Java Developer position",
            "companyName", "Microsoft",
            "tone", "professional"
        ));

        assertNotNull(result);
        assertEquals("success", result.get("status"));
        assertEquals("Microsoft", result.get("companyName"));
    }

    @Test
    void jobDiscoveryTool_shouldHaveTimeout() {
        assertEquals(30, jobDiscoveryTool.timeoutSeconds());
    }

    @Test
    void resumeTailoringTool_shouldHaveTimeout() {
        assertEquals(90, resumeTailoringTool.timeoutSeconds());
    }

    @Test
    void coverLetterGeneratorTool_shouldHaveTimeout() {
        assertEquals(60, coverLetterGeneratorTool.timeoutSeconds());
    }
}

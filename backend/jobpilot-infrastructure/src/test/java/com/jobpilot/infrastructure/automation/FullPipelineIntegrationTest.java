package com.jobpilot.infrastructure.automation;

import com.jobpilot.application.agent.ports.AiProviderPort;
import com.jobpilot.application.agent.ports.CandidateProfileRepository;
import com.jobpilot.application.agent.tools.*;
import com.jobpilot.domain.agent.CandidateProfile;
import com.jobpilot.domain.agent.CandidateProfileId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullPipelineIntegrationTest {

    @Mock private AiProviderPort aiProvider;
    @Mock private CandidateProfileRepository profileRepository;
    @InjectMocks private JobScoringTool jobScoringTool;
    @InjectMocks private ResumeTailoringTool resumeTailoringTool;
    @InjectMocks private CoverLetterTool coverLetterTool;
    @InjectMocks private AtsResumeAnalyzerTool atsAnalyzerTool;
    @InjectMocks private InterviewPrepTool interviewPrepTool;
    @InjectMocks private CompanyIntelligenceTool companyIntelTool;

    private final PortalDetector portalDetector = new PortalDetector();

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private static final String SAMPLE_RESUME = """
        John Doe
        Software Engineer
        Email: john@example.com | Phone: +1-555-0123
        Location: San Francisco, CA

        Summary:
        Senior Software Engineer with 8 years of experience in Java, Spring Boot, and cloud technologies.

        Skills:
        Java, Spring Boot, PostgreSQL, React, AWS, Docker, Kubernetes, REST APIs, Microservices

        Experience:
        Software Engineer at Google (2019-2023)
        - Led development of microservices handling 10M+ requests/day
        - Reduced API latency by 40% through caching optimization

        Education:
        B.S. Computer Science, Stanford University (2012-2016)
        """;

    private static final String SAMPLE_JOB_DESCRIPTION = """
        Senior Software Engineer - Backend
        Company: TechCorp Inc
        Location: Remote (US)

        Requirements:
        - 5+ years of experience with Java or similar backend languages
        - Experience with Spring Boot and microservices architecture
        - Strong knowledge of SQL databases (PostgreSQL preferred)
        - Experience with cloud platforms (AWS/GCP)
        - Experience with Docker and Kubernetes
        """;

    @BeforeEach
    void setUp() {
        var profile = CandidateProfile.reconstitute(
            CandidateProfileId.generate(), TEST_USER_ID, "John Doe", "john@example.com",
            "+1-555-0123", "San Francisco, CA", "Senior Software Engineer",
            "Senior Software Engineer with 8 years of experience",
            List.of("Java", "Spring Boot", "PostgreSQL", "AWS", "Docker", "Kubernetes"),
            List.of("Software Engineer at Google (2019-2023)"),
            List.of("B.S. Computer Science, Stanford University"),
            List.of(), SAMPLE_RESUME, null, null, null, 8,
            "Senior Software Engineer", "Remote", 150000, 200000, "USD",
            "Full-time", "Remote", null, Instant.now(), Instant.now()
        );
        lenient().when(profileRepository.findByUserId(TEST_USER_ID)).thenReturn(Optional.of(profile));
    }

    @Test
    @Order(1)
    @DisplayName("Portal Detection - Identifies portal types from URLs")
    void testPortalDetection() {
        assertEquals("greenhouse", portalDetector.detectPortal("https://boards.greenhouse.io/company/jobs/12345"));
        assertEquals("lever", portalDetector.detectPortal("https://jobs.lever.co/company/abc123"));
        assertEquals("workday", portalDetector.detectPortal("https://company.wd5.myworkdayjobs.com/External/job/123"));
        assertEquals("icims", portalDetector.detectPortal("https://company.icims.com/jobs/12345"));
        assertEquals("taleo", portalDetector.detectPortal("https://company.taleo.net/careersection/123/job.ftl"));
        assertEquals("ashby", portalDetector.detectPortal("https://jobs.ashbyhq.com/company"));
        assertEquals("bamboohr", portalDetector.detectPortal("https://company.bamboohr.com/jobs"));
        assertEquals("smartrecruiters", portalDetector.detectPortal("https://careers.smartrecruiters.com/company"));
        assertEquals("unknown", portalDetector.detectPortal("https://example.com/apply"));

        assertTrue(portalDetector.isApplicationForm("https://boards.greenhouse.io/company/applications/123"));
        assertFalse(portalDetector.isApplicationForm("https://example.com/jobs"));
    }

    @Test
    @Order(2)
    @DisplayName("Job Scoring - AI scores jobs 0-100")
    void testJobScoring() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"score": 85, "matchReasons": ["Java experience"], "gapReasons": [], "matchingSkills": ["Java","Spring Boot"], "missingSkills": [], "recommendation": "apply"}
                """);

        Map<String, Object> input = Map.of(
            "title", "Senior Software Engineer",
            "company", "TechCorp Inc",
            "description", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );

        Map<String, Object> result = jobScoringTool.execute(input);
        assertTrue(result.containsKey("score"), "Result should contain 'score': " + result);
        int score = (int) result.get("score");
        assertTrue(score >= 0 && score <= 100, "Score should be 0-100 but was " + score);
        assertEquals("apply", result.get("recommendation"));
    }

    @Test
    @Order(3)
    @DisplayName("Resume Tailoring - AI rewrites resume for job")
    void testResumeTailoring() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("TAILORED: John Doe - Backend Specialist with Spring Boot expertise for TechCorp...");

        Map<String, Object> input = Map.of(
            "title", "Senior Software Engineer",
            "company", "TechCorp Inc",
            "description", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );

        Map<String, Object> result = resumeTailoringTool.execute(input);
        assertTrue(result.containsKey("tailoredResume"), "Result should contain 'tailoredResume': " + result);
        assertTrue(((String) result.get("tailoredResume")).length() > 50);
    }

    @Test
    @Order(4)
    @DisplayName("Cover Letter - AI generates personalized letter")
    void testCoverLetter() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("Dear Hiring Manager, I am excited to apply for the Senior Software Engineer position at TechCorp Inc. With 8 years of Java experience...");

        Map<String, Object> input = Map.of(
            "title", "Senior Software Engineer",
            "company", "TechCorp Inc",
            "description", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );

        Map<String, Object> result = coverLetterTool.execute(input);
        assertTrue(result.containsKey("coverLetter"), "Result should contain 'coverLetter': " + result);
        assertTrue(((String) result.get("coverLetter")).length() > 100);
    }

    @Test
    @Order(5)
    @DisplayName("ATS Analysis - Scores resume ATS compatibility")
    void testAtsAnalysis() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"atsScore": 78, "strengths": ["Good keywords"], "improvements": ["Add more keywords"]}
                """);

        Map<String, Object> input = Map.of(
            "resume", SAMPLE_RESUME,
            "jobDescription", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );
        Map<String, Object> result = atsAnalyzerTool.execute(input);
        assertTrue(result.containsKey("atsScore"), "Result should contain 'atsScore': " + result);
        assertTrue((int) result.get("atsScore") >= 0);
    }

    @Test
    @Order(6)
    @DisplayName("Interview Prep - Generates questions and answers")
    void testInterviewPrep() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"technicalQuestions": [{"question": "Explain microservices", "answer": "Microservices..."}], "behavioralQuestions": [], "salaryNegotiation": {"range": "$170k-$200k"}}
                """);

        Map<String, Object> input = Map.of(
            "title", "Senior Software Engineer",
            "company", "TechCorp Inc",
            "description", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );
        Map<String, Object> result = interviewPrepTool.execute(input);
        assertTrue(result.containsKey("technicalQuestions"), "Result should contain 'technicalQuestions': " + result);
    }

    @Test
    @Order(7)
    @DisplayName("Company Intelligence - Researches company culture and tech")
    void testCompanyIntelligence() {
        when(aiProvider.executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"company": "TechCorp Inc", "culture": "Remote-first", "techStack": ["Java", "Spring Boot"], "interviewProcess": ["Phone screen", "Technical"]}
                """);

        Map<String, Object> input = Map.of(
            "company", "TechCorp Inc",
            "description", SAMPLE_JOB_DESCRIPTION,
            "userId", TEST_USER_ID
        );
        Map<String, Object> result = companyIntelTool.execute(input);
        assertTrue(result.containsKey("company"), "Result should contain 'company': " + result);
    }

    @Test
    @Order(8)
    @DisplayName("Full Pipeline - Score -> Tailor -> Cover Letter -> Analyze")
    void testFullPipeline() {
        // Step 1: Score the job
        when(aiProvider.executePrompt(contains("Score"), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"score": 82, "matchReasons": ["Strong Java"], "gapReasons": [], "matchingSkills": ["Java"], "missingSkills": [], "recommendation": "apply"}
                """);

        Map<String, Object> scoreResult = jobScoringTool.execute(Map.of(
            "title", "Senior Software Engineer", "company", "TechCorp",
            "description", SAMPLE_JOB_DESCRIPTION, "userId", TEST_USER_ID
        ));
        assertTrue(scoreResult.containsKey("score"), "Score result: " + scoreResult);
        assertTrue((int) scoreResult.get("score") >= 60, "Score must be >= 60 to proceed");

        // Step 2: Tailor resume
        when(aiProvider.executePrompt(contains("tailor"), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("TAILORED RESUME for TechCorp Senior Software Engineer role with Spring Boot focus...");
        Map<String, Object> tailorResult = resumeTailoringTool.execute(Map.of(
            "title", "Senior Software Engineer", "company", "TechCorp",
            "description", SAMPLE_JOB_DESCRIPTION, "userId", TEST_USER_ID
        ));
        assertTrue(tailorResult.containsKey("tailoredResume"), "Tailor result: " + tailorResult);

        // Step 3: Generate cover letter
        when(aiProvider.executePrompt(contains("cover"), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("Dear Hiring Manager, I am thrilled to apply for the Senior Software Engineer position at TechCorp...");
        Map<String, Object> coverResult = coverLetterTool.execute(Map.of(
            "title", "Senior Software Engineer", "company", "TechCorp",
            "description", SAMPLE_JOB_DESCRIPTION, "userId", TEST_USER_ID
        ));
        assertTrue(coverResult.containsKey("coverLetter"), "Cover result: " + coverResult);

        // Step 4: ATS analysis
        when(aiProvider.executePrompt(contains("ATS"), anyString(), any(), anyDouble(), anyInt()))
            .thenReturn("""
                {"atsScore": 80, "strengths": ["Good match"], "improvements": ["Add CI/CD keywords"]}
                """);
        Map<String, Object> atsResult = atsAnalyzerTool.execute(Map.of(
            "resume", SAMPLE_RESUME, "jobDescription", SAMPLE_JOB_DESCRIPTION, "userId", TEST_USER_ID
        ));
        assertTrue(atsResult.containsKey("atsScore"), "ATS result: " + atsResult);
        assertTrue((int) atsResult.get("atsScore") >= 70);

        verify(aiProvider, atLeast(4)).executePrompt(anyString(), anyString(), any(), anyDouble(), anyInt());

        System.out.println("=== FULL PIPELINE PASSED ===");
        System.out.println("Score: " + scoreResult.get("score"));
        System.out.println("ATS: " + atsResult.get("atsScore"));
        System.out.println("Recommendation: " + scoreResult.get("recommendation"));
    }
}

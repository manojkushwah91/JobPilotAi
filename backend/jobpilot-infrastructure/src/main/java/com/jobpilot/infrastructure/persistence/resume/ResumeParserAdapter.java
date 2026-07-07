package com.jobpilot.infrastructure.persistence.resume;

import com.jobpilot.application.resume.ports.ResumeParserPort;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResumeParserAdapter implements ResumeParserPort {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserAdapter.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{2,4}\\)?[-.\\s]?)?\\d{3,4}[-.\\s]?\\d{3,4}");
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w.-]+\\.[a-zA-Z]{2,}[/\\w.-]*");
    private static final Pattern LINKEDIN_PATTERN = Pattern.compile(
        "linkedin\\.com/in/[\\w-]+");
    private static final Pattern GITHUB_PATTERN = Pattern.compile(
        "github\\.com/[\\w-]+");

    private static final Set<String> SECTION_HEADERS = Set.of(
        "summary", "objective", "profile", "about",
        "experience", "work experience", "employment", "professional experience", "work history",
        "education", "academic", "qualifications",
        "skills", "technical skills", "core competencies", "technologies", "tech stack",
        "certifications", "certificates", "licenses",
        "projects", "portfolio", "personal projects",
        "languages", "awards", "honors", "achievements",
        "publications", "research", "references", "volunteer", "interests"
    );

    @Override
    public String extractText(byte[] fileContent, String filename) {
        try {
            var lower = filename.toLowerCase();
            if (lower.endsWith(".pdf")) {
                return extractFromPdf(fileContent);
            } else if (lower.endsWith(".docx")) {
                return extractFromDocx(fileContent);
            } else if (lower.endsWith(".doc")) {
                return extractFromDocx(fileContent);
            } else if (lower.endsWith(".txt")) {
                return new String(fileContent);
            }
            throw new UnsupportedOperationException("Unsupported file type: " + filename);
        } catch (Exception e) {
            log.error("Failed to extract text from {}: {}", filename, e.getMessage());
            throw new RuntimeException("Failed to parse resume file: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> parseStructured(String text) {
        var result = new LinkedHashMap<String, Object>();

        result.put("email", extractEmail(text));
        result.put("phone", extractPhone(text));
        result.put("linkedinUrl", extractLinkedIn(text));
        result.put("githubUrl", extractGithub(text));
        result.put("urls", extractUrls(text));
        result.put("sections", extractSections(text));
        result.put("skills", extractSkillsFromText(text));
        result.put("yearsExperience", estimateYearsExperience(text));

        return result;
    }

    private String extractFromPdf(byte[] content) throws Exception {
        try (var doc = Loader.loadPDF(content)) {
            var stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractFromDocx(byte[] content) throws Exception {
        try (var doc = new XWPFDocument(new ByteArrayInputStream(content));
             var extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractEmail(String text) {
        var matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractPhone(String text) {
        var matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            var phone = matcher.group().trim();
            var digits = phone.replaceAll("[^\\d]", "");
            if (digits.length() >= 7 && digits.length() <= 15) {
                return phone;
            }
        }
        return null;
    }

    private String extractLinkedIn(String text) {
        var matcher = LINKEDIN_PATTERN.matcher(text);
        if (matcher.find()) {
            return "https://www." + matcher.group();
        }
        return null;
    }

    private String extractGithub(String text) {
        var matcher = GITHUB_PATTERN.matcher(text);
        if (matcher.find()) {
            return "https://" + matcher.group();
        }
        return null;
    }

    private List<String> extractUrls(String text) {
        var urls = new ArrayList<String>();
        var matcher = URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    private List<Map<String, String>> extractSections(String text) {
        var sections = new ArrayList<Map<String, String>>();
        var lines = text.split("\\n");
        String currentSection = null;
        var currentContent = new StringBuilder();

        for (var line : lines) {
            var trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            var lower = trimmed.toLowerCase().replaceAll("[^a-z\\s]", "").trim();
            if (isSectionHeader(lower) && trimmed.length() < 50) {
                if (currentSection != null) {
                    sections.add(Map.of(
                        "type", currentSection,
                        "title", currentSection,
                        "content", currentContent.toString().trim()
                    ));
                }
                currentSection = mapSectionType(lower);
                currentContent.setLength(0);
            } else if (currentSection != null) {
                currentContent.append(line).append("\n");
            }
        }

        if (currentSection != null) {
            sections.add(Map.of(
                "type", currentSection,
                "title", currentSection,
                "content", currentContent.toString().trim()
            ));
        }

        return sections;
    }

    private boolean isSectionHeader(String line) {
        return SECTION_HEADERS.contains(line);
    }

    private String mapSectionType(String header) {
        return switch (header) {
            case "summary", "objective", "profile", "about" -> "SUMMARY";
            case "experience", "work experience", "employment",
                 "professional experience", "work history" -> "EXPERIENCE";
            case "education", "academic", "qualifications" -> "EDUCATION";
            case "skills", "technical skills", "core competencies",
                 "technologies", "tech stack" -> "SKILLS";
            case "certifications", "certificates", "licenses" -> "CERTIFICATIONS";
            case "projects", "portfolio", "personal projects" -> "PROJECTS";
            case "languages" -> "LANGUAGES";
            case "awards", "honors", "achievements" -> "AWARDS";
            case "publications", "research" -> "PUBLICATIONS";
            default -> "CUSTOM";
        };
    }

    private List<String> extractSkillsFromText(String text) {
        var skills = new ArrayList<String>();
        var commonTech = List.of(
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "node", "spring", "django", "flask", "fastapi",
            "aws", "azure", "gcp", "docker", "kubernetes", "k8s",
            "postgresql", "mysql", "mongodb", "redis", "elasticsearch",
            "git", "jenkins", "ci/cd", "terraform", "ansible",
            "microservices", "rest", "graphql", "grpc",
            "machine learning", "deep learning", "nlp", "llm", "ai",
            "html", "css", "sass", "tailwind",
            "linux", "bash", "powershell",
            "agile", "scrum", "jira",
            "c\\+\\+", "c#", ".net", "go", "golang", "rust", "ruby", "php",
            "swift", "kotlin", "scala", "r", "matlab",
            "tableau", "power bi", "excel", "sql",
            "opencv", "tensorflow", "pytorch", "keras", "scikit-learn",
            "spring boot", "hibernate", "maven", "gradle",
            "next.js", "nextjs", "nuxt", "svelte",
            "postgres", "mariadb", "cassandra", "dynamodb", "firebase",
            "kafka", "rabbitmq", "activemq",
            "prometheus", "grafana", "datadog", "splunk",
            "figma", "sketch", "adobe xd"
        );

        var lowerText = text.toLowerCase();
        for (var skill : commonTech) {
            if (lowerText.contains(skill)) {
                skills.add(skill);
            }
        }

        return skills;
    }

    private int estimateYearsExperience(String text) {
        var pattern = Pattern.compile(
            "(\\d{1,2})\\s*(?:\\+\\s*)?years?(?:\\s+of)?\\s+(?:experience|exp)",
            Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        var datePattern = Pattern.compile(
            "(?:20[0-2]\\d|199\\d)\\s*[-–]\\s*(?:present|20[0-2]\\d|current)",
            Pattern.CASE_INSENSITIVE);
        var dateMatcher = datePattern.matcher(text);
        int count = 0;
        while (dateMatcher.find()) {
            count++;
        }
        return count > 0 ? Math.min(count * 2, 30) : 0;
    }
}

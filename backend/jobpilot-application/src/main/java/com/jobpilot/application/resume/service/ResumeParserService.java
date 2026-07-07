package com.jobpilot.application.resume.service;

import com.jobpilot.application.resume.dto.ParsedResumeResponse;
import com.jobpilot.application.resume.ports.ResumeParserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ResumeParserService {

    private static final Logger log = LoggerFactory.getLogger(ResumeParserService.class);

    private final ResumeParserPort parserPort;

    public ResumeParserService(ResumeParserPort parserPort) {
        this.parserPort = parserPort;
    }

    @SuppressWarnings("unchecked")
    public ParsedResumeResponse parse(byte[] fileContent, String filename) {
        log.info("Parsing resume: {}", filename);

        var text = parserPort.extractText(fileContent, filename);
        var structured = parserPort.parseStructured(text);

        List<Map<String, String>> sections = List.of();
        if (structured.get("sections") instanceof List<?> s) {
            sections = s.stream()
                .filter(Map.class::isInstance)
                .map(o -> (Map<String, String>) o)
                .toList();
        }

        List<String> skills = List.of();
        if (structured.get("skills") instanceof List<?> sk) {
            skills = sk.stream()
                .map(Object::toString)
                .toList();
        }

        return new ParsedResumeResponse(
            text,
            (String) structured.get("email"),
            (String) structured.get("phone"),
            (String) structured.get("linkedinUrl"),
            (String) structured.get("githubUrl"),
            skills,
            sections,
            structured.get("yearsExperience") instanceof Integer y ? y : 0
        );
    }
}

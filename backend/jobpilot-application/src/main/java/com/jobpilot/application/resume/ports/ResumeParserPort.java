package com.jobpilot.application.resume.ports;

import java.util.Map;

public interface ResumeParserPort {

    String extractText(byte[] fileContent, String filename);

    Map<String, Object> parseStructured(String text);
}

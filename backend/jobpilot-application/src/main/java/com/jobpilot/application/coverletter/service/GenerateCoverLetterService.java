package com.jobpilot.application.coverletter.service;

import com.jobpilot.application.ai.ports.AiProviderPort;
import com.jobpilot.application.ai.ports.PromptTemplateRepository;
import com.jobpilot.application.coverletter.dto.CoverLetterResponse;
import com.jobpilot.application.coverletter.dto.GenerateCoverLetterCommand;
import com.jobpilot.application.coverletter.ports.CoverLetterRepository;
import com.jobpilot.application.coverletter.usecase.GenerateCoverLetterUseCase;
import com.jobpilot.domain.coverletter.CoverLetter;
import com.jobpilot.domain.coverletter.CoverLetterId;
import com.jobpilot.domain.identity.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GenerateCoverLetterService implements GenerateCoverLetterUseCase {

    private final CoverLetterRepository coverLetterRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final AiProviderPort aiProvider;

    public GenerateCoverLetterService(CoverLetterRepository coverLetterRepository,
                                      PromptTemplateRepository promptTemplateRepository,
                                      AiProviderPort aiProvider) {
        this.coverLetterRepository = coverLetterRepository;
        this.promptTemplateRepository = promptTemplateRepository;
        this.aiProvider = aiProvider;
    }

    @Override
    public CoverLetterResponse execute(GenerateCoverLetterCommand command) {
        var template = promptTemplateRepository.findActiveByUseCase("cover_letter")
            .orElseThrow(() -> new RuntimeException("Cover letter prompt template not found"));

        var systemPrompt = template.systemPrompt();
        var userPrompt = template.userPromptTemplate()
            .replace("{{company_name}}", command.companyName())
            .replace("{{job_title}}", command.jobTitle())
            .replace("{{recipient_name}}", command.recipientName() != null ? command.recipientName() : "Hiring Manager");

        var generated = aiProvider.executePrompt(systemPrompt, userPrompt, "gpt-4", 0.7, 2000);

        var coverLetterId = CoverLetterId.generate();
        var userId = UserId.from(command.userId());
        var coverLetter = CoverLetter.create(coverLetterId, userId, "Cover Letter for " + command.companyName());
        coverLetter.updateContent(generated);
        coverLetter.updateCompanyName(command.companyName());
        if (command.tone() != null) {
            coverLetter.updateTone(command.tone());
        }
        if (command.recipientName() != null) {
            coverLetter.updateRecipientName(command.recipientName());
        }
        coverLetter.markAiGenerated();

        coverLetterRepository.save(coverLetter);
        return CoverLetterResponse.from(coverLetter);
    }
}

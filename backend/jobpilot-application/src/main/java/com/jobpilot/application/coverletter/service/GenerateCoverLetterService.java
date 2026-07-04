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

import java.util.UUID;

@Service
@Transactional
public class GenerateCoverLetterService implements GenerateCoverLetterUseCase {

    private final AiProviderPort aiProvider;
    private final PromptTemplateRepository promptTemplateRepository;
    private final CoverLetterRepository coverLetterRepository;

    public GenerateCoverLetterService(AiProviderPort aiProvider,
                                      PromptTemplateRepository promptTemplateRepository,
                                      CoverLetterRepository coverLetterRepository) {
        this.aiProvider = aiProvider;
        this.promptTemplateRepository = promptTemplateRepository;
        this.coverLetterRepository = coverLetterRepository;
    }

    @Override
    public CoverLetterResponse execute(GenerateCoverLetterCommand command) {
        var title = "Cover Letter - " + command.companyName();
        var coverLetterId = CoverLetterId.generate();
        var userId = UserId.from(command.userId());
        var coverLetter = CoverLetter.create(coverLetterId, userId, title);
        coverLetter.updateCompanyName(command.companyName());
        if (command.recipientName() != null) {
            coverLetter.updateRecipientName(command.recipientName());
        }
        if (command.tone() != null) {
            coverLetter.updateTone(command.tone());
        }

        var template = promptTemplateRepository.findActiveByUseCase("cover_letter")
            .orElse(null);

        String content;
        if (template != null) {
            var userPrompt = template.userPromptTemplate()
                .replace("{{companyName}}", command.companyName())
                .replace("{{jobTitle}}", command.jobTitle())
                .replace("{{recipientName}}", command.recipientName() != null ? command.recipientName() : "Hiring Manager")
                .replace("{{tone}}", command.tone() != null ? command.tone() : "PROFESSIONAL");
            content = aiProvider.executePrompt(template.systemPrompt(), userPrompt,
                template.model(), template.temperature(), template.maxTokens());
        } else {
            var system = "You are a professional cover letter writer. Write a compelling cover letter based on the details provided. Use proper business letter format. Return only the letter body, no metadata.";
            var user = "Write a " + (command.tone() != null ? command.tone().toLowerCase() : "professional")
                + " cover letter for " + command.companyName()
                + " for the position of " + command.jobTitle()
                + (command.recipientName() != null ? ". Recipient: " + command.recipientName() : "");
            content = aiProvider.executePrompt(system, user, null, 0.7, 1500);
        }

        coverLetter.updateContent(content != null ? content.trim() : "");
        coverLetter.markAiGenerated();

        coverLetterRepository.save(coverLetter);
        return CoverLetterResponse.from(coverLetter);
    }
}

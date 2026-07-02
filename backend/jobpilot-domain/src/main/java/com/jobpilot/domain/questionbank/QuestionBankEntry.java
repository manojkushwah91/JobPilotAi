package com.jobpilot.domain.questionbank;

import com.jobpilot.domain.shared.BaseAggregateRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestionBankEntry extends BaseAggregateRoot {

    private final QuestionBankId questionBankId;
    private String type;
    private String category;
    private String question;
    private int difficulty;
    private String expectedAnswer;
    private List<String> tags;
    private String source;
    private UUID companyId;
    private int timesUsed;

    private QuestionBankEntry(QuestionBankId questionBankId, String type, String category, String question, int difficulty) {
        super(questionBankId.value());
        this.questionBankId = questionBankId;
        this.type = type;
        this.category = category;
        this.question = question;
        this.difficulty = difficulty;
        this.tags = new ArrayList<>();
        this.timesUsed = 0;
    }

    public static QuestionBankEntry create(QuestionBankId questionBankId, String type, String category, String question, int difficulty) {
        return new QuestionBankEntry(questionBankId, type, category, question, difficulty);
    }

    public static QuestionBankEntry reconstitute(QuestionBankId questionBankId, String type, String category,
                                                  String question, int difficulty, String expectedAnswer,
                                                  List<String> tags, String source, UUID companyId,
                                                  int timesUsed) {
        var entry = new QuestionBankEntry(questionBankId, type, category, question, difficulty);
        entry.expectedAnswer = expectedAnswer;
        entry.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        entry.source = source;
        entry.companyId = companyId;
        entry.timesUsed = timesUsed;
        return entry;
    }

    public void recordUsage() {
        timesUsed++;
    }

    public QuestionBankId questionBankId() { return questionBankId; }
    public String type() { return type; }
    public String category() { return category; }
    public String question() { return question; }
    public int difficulty() { return difficulty; }
    public String expectedAnswer() { return expectedAnswer; }
    public List<String> tags() { return List.copyOf(tags); }
    public String source() { return source; }
    public UUID companyId() { return companyId; }
    public int timesUsed() { return timesUsed; }
}

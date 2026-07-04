package com.jobpilot.application.automation.workflow;

import com.jobpilot.application.automation.ports.PlaywrightAutomationPort;
import com.jobpilot.domain.automation.AutomationSession;
import com.jobpilot.domain.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AutoApplyWorkflow extends AutomationWorkflow {

    private static final Logger log = LoggerFactory.getLogger(AutoApplyWorkflow.class);

    private final String applicationUrl;
    private final User user;

    public AutoApplyWorkflow(PlaywrightAutomationPort playwright, String applicationUrl, User user) {
        super(playwright);
        this.applicationUrl = applicationUrl;
        this.user = user;
        buildSteps();
    }

    private void buildSteps() {
        addStep(new WorkflowStep() {
            @Override public void execute(AutomationSession session) { playwright.navigateTo(applicationUrl); }
            @Override public String description() { return "Navigating to application page"; }
        });

        addStep(new WorkflowStep() {
            @Override public void execute(AutomationSession session) {
                playwright.waitForSelector("form, input, textarea, [type=submit]");
            }
            @Override public String description() { return "Waiting for form to load"; }
        });

        addStep(new WorkflowStep() {
            @Override public void execute(AutomationSession session) {
                var html = playwright.getHtml();
                var fieldsJson = playwright.evaluate(
                    "() => JSON.stringify(Array.from(document.querySelectorAll('input:not([type=hidden]):not([type=submit]):not([type=file]):not([type=checkbox]):not([type=radio]), textarea, select')).map(el => ({" +
                    "  name: el.name || ''," +
                    "  id: el.id || ''," +
                    "  type: el.type || 'text'," +
                    "  placeholder: el.placeholder || ''," +
                    "  label: ((el.closest('label')?.textContent || document.querySelector('label[for=\"' + el.id + '\"]')?.textContent || '').trim())," +
                    "  ariaLabel: el.getAttribute('aria-label') || ''" +
                    "})))"
                );

                List<Map<String, Object>> fields;
                try {
                    var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    fields = mapper.readValue(fieldsJson, List.class);
                } catch (Exception e) {
                    log.warn("Failed to parse form fields: {}", e.getMessage());
                    return;
                }

                for (var field : fields) {
                    var name = str(field, "name");
                    var id = str(field, "id");
                    var placeholder = str(field, "placeholder").toLowerCase();
                    var label = str(field, "label").toLowerCase();
                    var ariaLabel = str(field, "ariaLabel").toLowerCase();
                    var type = str(field, "type");
                    var combined = (name + " " + id + " " + placeholder + " " + label + " " + ariaLabel).toLowerCase();
                    var selector = buildSelector(name, id);

                    if (selector == null) continue;
                    if (type.equals("file")) continue;

                    var value = resolveValue(combined);
                    if (value != null) {
                        try {
                            playwright.fillField(selector, value);
                            log.info("Filled '{}' with '{}'", combined.substring(0, Math.min(combined.length(), 40)), value);
                        } catch (Exception e) {
                            log.warn("Could not fill field '{}': {}", combined, e.getMessage());
                        }
                    }
                }
            }
            @Override public String description() { return "Filling application form"; }
        });

        addStep(new WorkflowStep() {
            @Override public void execute(AutomationSession session) {
                var js = """
                    (() => {
                        var all = Array.from(document.querySelectorAll(
                            'button, input[type=submit], a[href]'
                        ));
                        var btns = all.filter(function(b) {
                            var txt = (b.textContent || b.value || '').trim().toLowerCase();
                            return txt.includes('submit') || txt.includes('apply') ||
                                   txt.includes('send') || txt.includes('next');
                        });
                        for (var b of btns) {
                            var txt = (b.textContent || b.value || '').trim();
                            b.click();
                            return txt;
                        }
                        var lastBtn = all.filter(function(b) {
                            return b.tagName === 'BUTTON' || b.type === 'submit';
                        });
                        if (lastBtn.length > 0) { lastBtn[lastBtn.length - 1].click(); return 'fallback'; }
                        return '';
                    })()
                """;
                var clicked = playwright.evaluate(js);
                log.info("Clicked submit button: {}", clicked);
            }
            @Override public String description() { return "Submitting application"; }
        });

        addStep(new WorkflowStep() {
            @Override public void execute(AutomationSession session) {
                playwright.waitForTimeout(3000);
                var currentUrl = playwright.getCurrentUrl();
                log.info("After submit, URL: {}", currentUrl);
            }
            @Override public String description() { return "Waiting for confirmation"; }
        });
    }

    private String str(Map<String, Object> map, String key) {
        var val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private String buildSelector(String name, String id) {
        if (!id.isBlank()) return "#" + cssEscape(id);
        if (!name.isBlank()) return "[name=\"" + cssEscape(name) + "\"]";
        return null;
    }

    private String cssEscape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("]", "\\]");
    }

    private String resolveValue(String combined) {
        var name = combined.toLowerCase();
        if (name.contains("first")) return user.name().split(" ")[0];
        if (name.contains("last")) return user.name().contains(" ") ? user.name().split(" ", 2)[1] : "";
        if (name.contains("email") || name.contains("e-mail")) return user.email().value();
        if (name.contains("phone") || name.contains("tel") || name.contains("mobile")) return "555-0100";
        if (name.contains("city") || name.contains("town")) return "Remote";
        if (name.contains("company") || name.contains("employer")) return "";
        if (name.contains("linkedin") || name.contains("portfolio") || name.contains("website") || name.contains("url")) return "";
        if (name.contains("github")) return "";
        if (name.contains("message") || name.contains("cover") || name.contains("note")) return "I am excited to apply for this position.";
        if (name.contains("salary") || name.contains("rate") || name.contains("compensation")) return "Negotiable";
        if (name.contains("hear") || name.contains("refer")) return "Job board";
        if (name.contains("start") || name.contains("available")) return "Immediately";
        if (name.contains("work")) return "Yes";
        if (name.contains("sponsor") || name.contains("visa")) return "No";
        return null;
    }
}
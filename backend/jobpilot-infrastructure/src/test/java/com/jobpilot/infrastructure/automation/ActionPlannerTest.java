package com.jobpilot.infrastructure.automation;

import com.jobpilot.domain.automation.BrowserSession;
import com.jobpilot.domain.automation.PageAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionPlannerTest {

    @Mock
    private PlaywrightBrowserManager browserManager;

    @Mock
    private PlaywrightDomAnalyzer domAnalyzer;

    @InjectMocks
    private ActionPlanner actionPlanner;

    private BrowserSession testSession;

    @BeforeEach
    void setUp() {
        testSession = BrowserSession.create(null, null, "LinkedIn");
    }

    @Test
    void shouldPlanLoginActions() {
        var actions = actionPlanner.planLogin(
            "https://linkedin.com/login",
            "#username",
            "#password",
            "button[type='submit']",
            "user@test.com",
            "password123"
        );

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
        assertEquals(PageAction.ActionType.NAVIGATE, actions.get(0).type());
    }

    @Test
    void shouldPlanSearchActions() {
        var actions = actionPlanner.planSearch(
            "https://linkedin.com/jobs/search/",
            Map.of("keywords", "Java Developer", "location", "Remote")
        );

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
        assertEquals(PageAction.ActionType.NAVIGATE, actions.get(0).type());
    }

    @Test
    void shouldPlanEasyApplyActions() {
        var actions = actionPlanner.planEasyApply("https://linkedin.com/jobs/view/12345");

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
        assertEquals(PageAction.ActionType.NAVIGATE, actions.get(0).type());
    }

    @Test
    void shouldPlanFormFillActions() {
        when(domAnalyzer.analyzeForm("form")).thenReturn(
            java.util.List.of(
                new com.jobpilot.application.automation.ports.DomAnalyzerPort.FormField(
                    "#name", "text", "Name", "name", true, "", null, Map.of()
                ),
                new com.jobpilot.application.automation.ports.DomAnalyzerPort.FormField(
                    "#email", "email", "Email", "email", true, "", null, Map.of()
                )
            )
        );

        var actions = actionPlanner.planFormFill("form", Map.of("name", "John", "email", "john@test.com"));

        assertNotNull(actions);
        assertEquals(2, actions.size());
        assertEquals(PageAction.ActionType.FILL, actions.get(0).type());
    }

    @Test
    void shouldPlanQuestionAnswers() {
        var answers = Map.of(
            "#question1", "Answer 1",
            "#question2", "Answer 2"
        );

        var actions = actionPlanner.planQuestionAnswers(answers);

        assertNotNull(actions);
        assertEquals(2, actions.size());
    }

    @Test
    void shouldPlanSubmitActions() {
        var actions = actionPlanner.planSubmit();

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
    }

    @Test
    void shouldPlanErrorRecoveryForTimeout() {
        var actions = actionPlanner.planErrorRecovery("timeout");

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
        assertEquals(PageAction.ActionType.SCREENSHOT, actions.get(0).type());
    }

    @Test
    void shouldPlanErrorRecoveryForElementNotFound() {
        var actions = actionPlanner.planErrorRecovery("element_not_found");

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
    }

    @Test
    void shouldPlanErrorRecoveryForNavigationError() {
        var actions = actionPlanner.planErrorRecovery("navigation_error");

        assertNotNull(actions);
        assertFalse(actions.isEmpty());
    }
}

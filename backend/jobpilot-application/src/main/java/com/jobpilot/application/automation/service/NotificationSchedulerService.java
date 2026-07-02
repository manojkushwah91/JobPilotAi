package com.jobpilot.application.automation.service;

import com.jobpilot.application.automation.dto.ScheduleNotificationCommand;
import com.jobpilot.application.automation.dto.ScheduledTaskResponse;
import com.jobpilot.application.automation.ports.ScheduledTaskRepository;
import com.jobpilot.application.automation.usecase.ScheduleNotificationUseCase;
import com.jobpilot.domain.automation.ScheduledTask;
import com.jobpilot.domain.automation.ScheduledTaskId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationSchedulerService implements ScheduleNotificationUseCase {

    private final ScheduledTaskRepository scheduledTaskRepository;

    public NotificationSchedulerService(ScheduledTaskRepository scheduledTaskRepository) {
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    @Override
    public ScheduledTaskResponse execute(ScheduleNotificationCommand command) {
        var taskId = ScheduledTaskId.generate();
        var payload = buildPayload(command);
        var task = ScheduledTask.schedule(
            taskId, command.userId(), "SEND_NOTIFICATION", payload, command.scheduledAt());
        scheduledTaskRepository.save(task);
        return ScheduledTaskResponse.from(task);
    }

    private String buildPayload(ScheduleNotificationCommand cmd) {
        return "{\"type\":\"" + escape(cmd.type())
            + "\",\"channel\":\"" + cmd.channel().name()
            + "\",\"title\":\"" + escape(cmd.title())
            + "\",\"body\":\"" + escape(cmd.body()) + "\"}";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.MessageId;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.hibernate.action.internal.EntityDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final NotificationTaskRepository notificationTaskRepository;
    @Autowired
    private TelegramBot telegramBot;

    public TelegramBotUpdatesListener(NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
    }


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message().text().equalsIgnoreCase("/start")) {
                checkValidStart(update);
            } else {
                    getTask(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void checkValidStart(Update update) {
        logger.info("Processing check valid start");
        SendMessage answerMessage = new SendMessage(update.message().chat().id(),
                "Hello my Dear friend @" + update.message().chat().username() +
                        "\nВведи то, что тебе необходимо напомнить в таком формате:" +
                        "\n01.01.2063 20:00 Покормить кибер кошку");
        telegramBot.execute(answerMessage);
    }

    public void getTask(Update update) {
        Pattern pattern = Pattern.compile("([0-9.:\\s]{16})(\\s)([\\W+|\\w]+)");
        String message = update.message().text();
        Matcher matcher = pattern.matcher(message);
        NotificationTask task = new NotificationTask();
        if (matcher.matches()) {
            String data = matcher.group(1);
            String taskText = matcher.group(3);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            try {
                LocalDateTime dataTime = LocalDateTime.parse(data, format);
                task.setChatId(update.message().chat().id());
                task.setTaskText(taskText);
                task.setNotificationTime(dataTime.truncatedTo(ChronoUnit.MINUTES));
                task.setTaskHead("Task " + taskText);
                createTask(task);
                telegramBot.execute(new SendMessage(task.getChatId(), "Задача сохранена"));
            } catch (Exception e) {
                telegramBot.execute(new SendMessage(update.message().chat().id(), "Неверный формат"));
            }
        }
    }
    // Запись задачи в БД
    public NotificationTask createTask(NotificationTask notificationTask) {
        logger.info("Create Task");
        return notificationTaskRepository.save(notificationTask);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void checkNotification () {
        try {
            logger.info("Check Notification time");
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            List<NotificationTask> taskToRemind = notificationTaskRepository
                    .findAllByNotificationTime(now);

            taskToRemind.forEach(task -> {
                Long taskChatId = task.getChatId();
                telegramBot.execute(new SendMessage(taskChatId, task.getTaskText()));
            });

        } catch (Exception e) {
            logger.info("No updates");
        }
    }
}

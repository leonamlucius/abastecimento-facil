package com.example.leonamlucius.compesa_facil.scheduler;

import com.example.leonamlucius.compesa_facil.service.UserService;
import com.example.leonamlucius.compesa_facil.service.BotService;
import com.example.leonamlucius.compesa_facil.dto.CalendarResponse;
import com.example.leonamlucius.compesa_facil.dto.UserResponse;
import com.example.leonamlucius.compesa_facil.service.CalendarService;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SendCalendar {

    private final UserService userService;
    private final BotService botService;
    private final CalendarService calendarService;

    @Scheduled(cron = "0 0 7 1 * ?")
    public void sendCalendar() {

        List<UserResponse> users = userService.getAllUsers();

        for (UserResponse user : users) {

            CalendarResponse calendarResponse = calendarService.takeScreenshot(user.andress());
            botService.sendImage(user.chatId(), "Aqui está o seu calendário do mês!", calendarResponse.screenshot());
        }

    }

}

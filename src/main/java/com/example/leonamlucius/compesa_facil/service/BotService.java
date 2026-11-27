package com.example.leonamlucius.compesa_facil.service;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.example.leonamlucius.compesa_facil.service.UserService;

import com.example.leonamlucius.compesa_facil.dto.CalendarResponse;

import tools.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.leonamlucius.compesa_facil.dto.UserRequest;
import com.example.leonamlucius.compesa_facil.dto.UserResponse;

@Service

public class BotService {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(BotService.class);

    private final CalendarService calendarioService;
    private final RestClient telegramClient;
    private final RestClient cepClient;
    private boolean hasAreadyAddress = false;
    private long offset = 0;

    private final String token;

    private final Set<Long> chatsAguardando = ConcurrentHashMap.newKeySet();

    public BotService(
            CalendarService calendarioService,
            @Value("${TELEGRAM_BOT_TOKEN}") String token,
            UserService userService) {

        this.userService = userService;
        this.calendarioService = calendarioService;
        this.token = token;
        this.telegramClient = RestClient.create("https://api.telegram.org/bot" + this.token);
        this.cepClient = RestClient.create("https://brasilapi.com.br/api/cep/v2");
    }

    @Scheduled(fixedDelay = 3000)
    public void verifyMessages() {

        JsonNode root = telegramClient.get().uri("/getUpdates?timeout=5&offset={offset}", offset).retrieve()
                .body(JsonNode.class);

        if (root == null || !root.path("ok").asBoolean()) {
            return;
        }
        for (JsonNode update : root.path("result")) {
            offset = update.path("update_id").asLong() + 1;
            JsonNode message = update.path("message");
            JsonNode callbackQuery = update.path("callback_query");

            long chatId = callbackQuery.path("message").path("chat").path("id").asLong() != 0
                    ? callbackQuery.path("message").path("chat").path("id").asLong()
                    : message.path("chat").path("id").asLong();

            String text = message.path("text").asText().trim();

            try {
                if (!callbackQuery.isMissingNode()) {

                    UserResponse user = userService.getUserByChatId(chatId);

                    String callbackAction = callbackQuery.path("data").asText();
                    switch (callbackAction) {
                        case "consultar_calendario":

                            if (user == null) {
                                sendMessage(chatId,
                                        "Nenhum endereço cadastrado. Por favor, cadastre um endereço primeiro.");
                                sendButtons(chatId);

                                break;
                            }

                            logger.info("Consultando rua do endereço cadastrado para o chat " + chatId
                                    + ": " + user.andress());

                            sendMessage(chatId, "Consultando calendário para " + user.andress() + "...");
                            CalendarResponse calendarResponse = calendarioService
                                    .takeScreenshot(user.andress());

                            byte[] calendarImage = calendarResponse.screenshot();
                            sendImage(chatId, "Aqui está o seu calendário:", calendarImage);
                            sendButtons(chatId);

                            if (calendarImage.length > 0) {
                                break;
                            }

                        case "cadastrar_endereco":

                            if (user != null) {
                                sendMessage(chatId,
                                        "Endereço já cadastrado. Para alterar, remova o endereço atual primeiro.");
                                sendButtons(chatId);
                                break;
                            }

                            if (hasAreadyAddress) {
                                hasAreadyAddress = false;
                            }
                            chatsAguardando.add(chatId);

                            break;

                        case "remover_endereco":

                            if (user == null) {
                                sendMessage(chatId, "Nenhum endereço cadastrado.");
                                sendButtons(chatId);
                                break;
                            }

                            sendButtonsDelete(chatId);

                            break;
                        case "cancelar_remocao":
                            sendMessage(chatId, "Remoção de endereço cancelada.");
                            sendButtons(chatId);
                            break;
                        case "confirmar_remocao":
                            userService.deleteByChatId(chatId);
                            sendMessage(chatId, "Endereço removido com sucesso.");
                            sendButtons(chatId);
                            break;
                    }

                }

                if (message.path("text").asText().equals("/start")) {
                    sendMessage(chatId, "Bem-vindo!");
                    sendButtons(chatId);
                    continue;
                }

                if (chatsAguardando.contains(chatId)) {
                    if (text.isEmpty()) {
                        sendMessage(chatId, "Digite o seu CEP sem pontos ou traços.");
                        continue;
                    }

                    if (text.length() != 8 || text.matches(".*-.*") || !text.matches("\\d+")) {
                        sendMessage(chatId,
                                "CEP inválido. Digite o seu CEP sem pontos ou traços, apenas números e 8 dígitos.");
                        sendButtons(chatId);
                        continue;

                    }
                    ;

                    JsonNode cepResponse = cepClient.get().uri("/{cep}", text).retrieve().body(JsonNode.class);

                    userService.createUser(new UserRequest(chatId, cepResponse.path("street").asText()));

                    chatsAguardando.remove(chatId);

                    sendMessage(chatId, "Endereço cadastrado com sucesso!");
                    sendButtons(chatId);
                }

            } catch (Exception e) {
                sendMessage(chatId, "Ocorreu um erro:");
                logger.error("Erro ao processar mensagem do chat " + chatId, e);
            }

        }
    }

    private void sendMessage(long chatId, String text) {
        telegramClient.get().uri("/sendMessage?chat_id={chatId}&text={text}", chatId, text).retrieve()
                .toBodilessEntity();
    }

    private void sendImage(long chatId, String caption, byte[] image) {

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("chat_id", String.valueOf(chatId));
        builder.part("caption", caption);
        builder.part("photo", new ByteArrayResource(image) {
            @Override
            public String getFilename() {
                return "calendar.jpg";
            }
        }).contentType(MediaType.IMAGE_JPEG);
        telegramClient.post().uri("/sendPhoto").contentType(MediaType.MULTIPART_FORM_DATA).body(builder.build())
                .retrieve().toBodilessEntity();

    }

    private void sendButtons(long chatId) {

        Map<String, Object> buttons = Map.of(
                "inline_keyboard", List.of(
                        List.of(
                                Map.of(
                                        "text", "Consultar calendário",
                                        "callback_data", "consultar_calendario")),

                        List.of(
                                Map.of(
                                        "text", "Cadastrar endereço",
                                        "callback_data", "cadastrar_endereco"))

                        ,
                        List.of(
                                Map.of(
                                        "text", "Remover endereço",
                                        "callback_data", "remover_endereco"))));

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", String.format("Escolha uma opção:"),
                "reply_markup", buttons);
        telegramClient.post().uri("/sendMessage").contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toBodilessEntity();

    }

    private void sendButtonsDelete(long chatId) {

        Map<String, Object> buttons = Map.of(
                "inline_keyboard", List.of(
                        List.of(
                                Map.of(
                                        "text", "Sim",
                                        "callback_data", "confirmar_remocao")),
                        List.of(
                                Map.of(
                                        "text", "Não",
                                        "callback_data", "cancelar_remocao"))

                ));

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", String.format("Deseja remover o endereço cadastrado?"),
                "reply_markup", buttons);
        telegramClient.post().uri("/sendMessage").contentType(MediaType.APPLICATION_JSON)
                .body(body).retrieve().toBodilessEntity();

    }

}

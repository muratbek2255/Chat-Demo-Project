package com.example.chatmessagner.config;

import com.example.chatmessagner.UserResponse;
import com.example.chatmessagner.Utils;
import com.example.chatmessagner.entity.User;
import com.example.chatmessagner.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import java.util.List;

public class WebSocketHandlerConnectionConfig extends WebSocketHandlerDecorator {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionService sessionService;

    public WebSocketHandlerConnectionConfig(
            WebSocketHandler delegate,
            SimpMessagingTemplate messagingTemplate,
            SessionService sessionService
    ) {
        super(delegate);

        this.messagingTemplate = messagingTemplate;
        this.sessionService = sessionService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        User user = Utils.user(session.getPrincipal());

        if (user != null) {
            Object payload = List.of(new UserResponse(user, true));

            sessionService.getContacts(user.getId()).stream()
                    .filter(UserResponse::isOnline)
                    .forEach(contact -> {
                        messagingTemplate.convertAndSendToUser(contact.getId().toString(), "/topic/contacts", payload);
                    });
        }
    }
}

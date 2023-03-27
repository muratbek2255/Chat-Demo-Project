package com.example.chatmessagner.controller;

import com.example.chatmessagner.Utils;
import com.example.chatmessagner.entity.Chat;
import com.example.chatmessagner.entity.User;
import com.example.chatmessagner.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


@Controller
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/chat/{chatId}/messages")
    public List<Chat> chatMessages(
            @AuthenticationPrincipal User user,
            @DestinationVariable("chatId") String chatId
    ) {
        if (!Utils.isChatMember(user.getId(), chatId))
            throw new AccessDeniedException("Not a chat member. UserId=" + user.getId() + ", chatId=" + chatId);

        return chatService.getLastMessages(chatId);
    }

    @MessageMapping("/chat/{contactId}/messages/add")
    public void sendMessage(
            @DestinationVariable("contactId") Long contactId,
            @AuthenticationPrincipal User user,
            String text
    ) {
        String chatId = Utils.chatId(user.getId(), contactId);
        Chat msg = chatService.saveMessage(user.getId(), contactId, text);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/messages", List.of(msg));
    }
}

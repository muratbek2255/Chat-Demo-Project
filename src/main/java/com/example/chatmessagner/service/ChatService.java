package com.example.chatmessagner.service;

import com.example.chatmessagner.entity.Chat;
import com.example.chatmessagner.repository.ChatRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static com.example.chatmessagner.Utils.chatId;


@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatMessageRepo;

    public List<Chat> getLastMessages(String chatId) {
        return chatMessageRepo.findAllByChatIdOrderByCreatedAtDesc(chatId, PageRequest.of(0, 10)).stream()
                .sorted(Comparator.comparing(Chat::getCreatedAt))
                .toList();
    }

    public Chat saveMessage(Integer authorId, Long contactId, String text) {
        String chatId = chatId(authorId, contactId);

        Chat msg = new Chat();

        msg.setChatId(chatId);
        msg.setAuthorId(authorId);

        return chatMessageRepo.save(msg);
    }
}

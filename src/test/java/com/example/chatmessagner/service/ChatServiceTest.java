package com.example.chatmessagner.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.chatmessagner.entity.Chat;
import com.example.chatmessagner.repository.ChatRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ChatServiceTest {
    @Mock
    private ChatRepository chatRepository;

    private ChatService chatService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatRepository);
    }

    @Test
    public void testGetLastMessages() {
        // Create a mock list of messages
        List<Chat> messages = List.of(
                new Chat(1, "1", 1, "Hello", LocalDateTime.now()),
                new Chat(2, "2", 2, "Hello", LocalDateTime.now()),
                new Chat(3, "1", 3, "Hello", LocalDateTime.now())
        );

        // Configure the mock repository to return the mock list of messages
        when(chatRepository.findAllByChatIdOrderByCreatedAtDesc(any(String.class), any(PageRequest.class)))
                .thenReturn(messages);

        // Call the method under test
        List<Chat> lastMessages = chatService.getLastMessages("1");

        // Verify that the correct list of messages is returned
        assertEquals(messages, lastMessages);
    }

    @Test
    public void testSaveMessage() {
        // Create a mock message
        Chat message = new Chat(1, "1", 1, "Hello", LocalDateTime.now());

        // Configure the mock repository to return the mock message when saving
        when(chatRepository.save(any(Chat.class))).thenReturn(message);

        // Call the method under test
        Chat savedMessage = chatService.saveMessage(1, 2L, "Hello");

        // Verify that the correct message is returned
        assertEquals(message, savedMessage);
    }
}

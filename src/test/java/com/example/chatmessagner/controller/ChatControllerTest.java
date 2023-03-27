package com.example.chatmessagner.controller;

import com.example.chatmessagner.entity.Chat;
import com.example.chatmessagner.entity.User;
import com.example.chatmessagner.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new User(1, "+996707899880", "$2a$10$23ilNBEI/m544dWzKyefuu4A5BX3QSj8PDRheEiUzHuAse/c/mkbK", "USER", Boolean.FALSE,
                                Timestamp.from(Instant.now())), null)
        );
    }

    @Test
    void chatMessages_shouldReturnLastMessages_whenUserIsChatMember() {
        // given
        User user = new User(1, "+996707899880", "$2a$10$23ilNBEI/m544dWzKyefuu4A5BX3QSj8PDRheEiUzHuAse/c/mkbK", "USER", Boolean.FALSE,
                Timestamp.from(Instant.now()));
        String chatId = "testChatId";
        List<Chat> expectedMessages = List.of(new Chat(1, "1", 1, "Hello", LocalDateTime.now()));

        when(chatService.getLastMessages(chatId)).thenReturn(expectedMessages);

        // when
        List<Chat> actualMessages = chatController.chatMessages(user, chatId);

        // then
        assertThat(actualMessages).isEqualTo(expectedMessages);
        verify(chatService).getLastMessages(chatId);
    }

    @Test
    void chatMessages_shouldThrowAccessDeniedException_whenUserIsNotChatMember() {
        // given
        User user = new User(1, "+996707899880", "$2a$10$23ilNBEI/m544dWzKyefuu4A5BX3QSj8PDRheEiUzHuAse/c/mkbK", "USER", Boolean.FALSE,
                Timestamp.from(Instant.now()));;
        String chatId = "testChatId";

        when(chatService.getLastMessages(chatId)).thenThrow(new AccessDeniedException("Not a chat member"));

        // when / then
        assertThatThrownBy(() -> chatController.chatMessages(user, chatId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Not a chat member");
        verify(chatService).getLastMessages(chatId);
    }

    @Test
    void sendMessage_shouldSaveAndSendMessage_whenCalled() {
        // given
        Long contactId = 2L;
        String text = "testMessage";
        Chat expectedChatMessage = new Chat(1, "1", 1, "Hello", LocalDateTime.now());

        when(chatService.saveMessage(anyInt(), eq(contactId), eq(text))).thenReturn(expectedChatMessage);

        // when
        User user = new User(1, "+996707899880", "$2a$10$23ilNBEI/m544dWzKyefuu4A5BX3QSj8PDRheEiUzHuAse/c/mkbK", "USER", Boolean.FALSE,
                Timestamp.from(Instant.now()));

        // then
        verify(chatService).saveMessage(1, eq(contactId), eq(text));
        verify(messagingTemplate).convertAndSend(eq("/topic/chat/testUser_2/messages"), eq(List.of(expectedChatMessage)));
    }
}

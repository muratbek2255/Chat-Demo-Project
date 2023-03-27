package com.example.chatmessagner.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@Table(name = "chat_messages")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    private String chatId;
    private Integer authorId;
    private String text;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Chat(int id, String chatId, int authorId, String text, LocalDateTime createdAt) {
    }

    public Chat() {

    }
}

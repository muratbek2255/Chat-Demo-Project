package com.example.chatmessagner.repository;

import com.example.chatmessagner.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

    List<Chat> findAllByChatIdOrderByCreatedAtDesc(String chatId, Pageable pageable);
}

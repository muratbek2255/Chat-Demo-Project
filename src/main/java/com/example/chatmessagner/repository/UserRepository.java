package com.example.chatmessagner.repository;

import com.example.chatmessagner.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO contacts (user1_id, user2_id) VALUES ((:user1Id, :user2Id), (:user2Id, :user1Id)) ON CONFLICT DO NOTHING", nativeQuery = true)
    void addContact(@Param("user1Id") Integer user1Id, @Param("user2Id") Integer user2Id);

    @Query(value = "SELECT u.* FROM users u LEFT JOIN contacts c ON u.id = c.user2_id WHERE c.user1_id = :userId", nativeQuery = true)
    List<User> findAllContacts(@Param("userId") Integer userId);

    User deleteByPhoneNumber(String phoneNumber);
}

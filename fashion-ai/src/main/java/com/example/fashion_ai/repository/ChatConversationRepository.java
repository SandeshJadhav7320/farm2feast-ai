package com.example.fashion_ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fashion_ai.entity.ChatConversation;

public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, Long> {

    Optional<ChatConversation> findByConversationIdAndCustomerId(
            String conversationId,
            Long customerId
    );

    boolean existsByConversationId(String conversationId);
}

package com.example.fashion_ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fashion_ai.entity.ChatMessage;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(
            String conversationId
    );
}

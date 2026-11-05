package com.example.fashion_ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.fashion_ai.entity.PendingCheckout;

public interface PendingCheckoutRepository
        extends JpaRepository<PendingCheckout, Long> {

    Optional<PendingCheckout>
    findFirstByCustomerIdAndConversationIdAndStatusOrderByCreatedAtDesc(
            Long customerId,
            String conversationId,
            String status
    );
}